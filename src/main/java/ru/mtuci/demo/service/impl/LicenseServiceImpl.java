package ru.mtuci.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.*;
import ru.mtuci.demo.repository.LicenseRepository;
import ru.mtuci.demo.service.*;

import java.security.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LicenseServiceImpl implements LicenseService {

    private LicenseRepository licenseRepository;
    private ProductService productService;
    private UserService userService;
    private LicenseTypeService licenseTypeService;
    private LicenseHistoryService licenseHistoryService;
    private DeviceLicenseService deviceLicenseService;

    @Override
    public ApplicationLicense createLicense(UUID productId,
                                            UUID ownerId,
                                            UUID licenseTypeId,
                                            Long count) {

        ApplicationLicenseType licenseType = licenseTypeService.getLicenseTypeById(licenseTypeId);
        ApplicationProduct product = productService.getProductById(productId);
        ApplicationLicense newLicense = new ApplicationLicense();

        String uuid = UUID.randomUUID().toString();

        while (licenseRepository.findByCode(uuid) != null) {
            uuid = UUID.randomUUID().toString();
        }

        newLicense.setCode(uuid);
        newLicense.setProduct(product);
        newLicense.setLicenseType(licenseType);
        newLicense.setBlocked(product.isBlocked());
        newLicense.setDeviceCount(count);
        newLicense.setOwnerId(userService.findUserById(ownerId));
        newLicense.setDuration(licenseType.getDefaultDuration());
        newLicense.setDescription(licenseType.getDescription());

        licenseRepository.save(newLicense);

        licenseHistoryService.recordLicenseChange(newLicense,
                userService.findUserById(ownerId),
                "Created");

        return newLicense;
    }

    @Override
    public ApplicationTicket activateLicense(String code,
                                             ApplicationDevice device,
                                             ApplicationUser user) {
        ApplicationTicket ticket = new ApplicationTicket();
        ApplicationLicense license = licenseRepository.findByCode(code);

        if (license == null) {
            ticket.setStatus("Error");
            ticket.setInfo("License not found");
            return ticket;
        }

        ApplicationLicense newLicense = license;
        if (newLicense.isBlocked() ||
                newLicense.getEndingDate() != null &&
                new Date().after(newLicense.getEndingDate()) ||
                newLicense.getUser() != null &&
                !Objects.equals(newLicense.getUser().getId(), user.getId()) ||
                deviceLicenseService.getDeviceCount(newLicense.getId()) >= newLicense.getDeviceCount()) {
            ticket.setStatus("Error");
            ticket.setInfo("Activation is not possible");
            return ticket;
        }

        if (newLicense.getFirstActivationDate() == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_MONTH, Math.toIntExact(newLicense.getDuration()));

            newLicense.setFirstActivationDate(new Date());
            newLicense.setEndingDate(calendar.getTime());
            newLicense.setUser(user);
        }

        deviceLicenseService.createDeviceLicense(newLicense, device);
        licenseRepository.save(newLicense);
        licenseHistoryService.recordLicenseChange(newLicense, user, "Activated");

        ticket = createTicket(user, device, newLicense, "Activated", "OK");

        return ticket;
    }

    @Override
    public String makeSignature(ApplicationTicket ticket)  {
        try{
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();
            ObjectMapper objectMapper = new ObjectMapper();
            String res = objectMapper.writeValueAsString(ticket);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(res.getBytes());

            return Base64.getEncoder().encodeToString(signature.sign());
        }
        catch (Exception e){
            return "Something went wrong. The signature is not valid";
        }
    }

    @Override
    public ApplicationTicket createTicket(ApplicationUser user,
                                          ApplicationDevice device,
                                          ApplicationLicense license,
                                          String info,
                                          String status) {
        ApplicationTicket ticket = new ApplicationTicket();
        ticket.setCurrentDate(new Date());

        if (user != null) {
            ticket.setUserId(user.getId());
        }

        if (device != null) {
            ticket.setDeviceId(device.getId());
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, 1);
        ticket.setLifetime(calendar.getTime());

        if (license != null) {
            ticket.setActivationDate(license.getFirstActivationDate());
            ticket.setExpirationDate(license.getEndingDate());
            ticket.setLicenseBlocked(license.isBlocked());
        }

        ticket.setInfo(info);
        ticket.setDigitalSignature(makeSignature(ticket));
        ticket.setStatus(status);

        return ticket;
    }

    @Override
    public ApplicationTicket getActiveLicensesForDevice(ApplicationDevice device, String code) {
        List<ApplicationDeviceLicense> applicationDeviceLicensesList = deviceLicenseService.getAllLicensesById(device);
        List<UUID> licenseIds = applicationDeviceLicensesList.stream()
                .map(license -> license.getLicense() != null ? license.getLicense().getId() : null)
                .collect(Collectors.toList());
        ApplicationLicense applicationLicense = licenseRepository.findByIdInAndCode(licenseIds, code);

        ApplicationTicket ticket = new ApplicationTicket();

        if (applicationLicense == null){
            ticket.setStatus("Error");
            ticket.setInfo("License was not found");

            return ticket;
        }
        ticket = createTicket(applicationLicense.getUser(), device, applicationLicense,
                "Info about license", "OK");

        return ticket;
    }

    @Override
    public List<ApplicationLicense> getLicensesByProductId(UUID productId) {
        return licenseRepository.findAllByProductId(productId);
    }

    @Override
    public ApplicationTicket renewLicense(String code, ApplicationUser user) {
        ApplicationTicket ticket = new ApplicationTicket();
        ApplicationLicense license = licenseRepository.findByCode(code);

        if (license == null) {
            ticket.setStatus("Error");
            ticket.setInfo("License was not found");

            return ticket;
        }

        ApplicationLicense newLicense = license;
        if (newLicense.isBlocked() ||
            newLicense.getEndingDate() != null &&
            new Date().after(newLicense.getEndingDate()) ||
            !Objects.equals(newLicense.getOwnerId().getId(), user.getId()) ||
            newLicense.getFirstActivationDate() == null) {
            ticket.setStatus("Error");
            ticket.setInfo("Failed to renew license");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(newLicense.getEndingDate());
        calendar.add(Calendar.DAY_OF_MONTH, Math.toIntExact(newLicense.getDuration()));
        newLicense.setEndingDate(calendar.getTime());

        licenseRepository.save(newLicense);
        licenseHistoryService.recordLicenseChange(newLicense, user, "Renewed");

        ticket = createTicket(user, null, newLicense, "Renewed", "OK");

        return ticket;
    }
}
