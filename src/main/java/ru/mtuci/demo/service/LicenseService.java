package ru.mtuci.demo.service;

import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationDevice;
import ru.mtuci.demo.model.ApplicationLicense;
import ru.mtuci.demo.model.ApplicationTicket;
import ru.mtuci.demo.model.ApplicationUser;

import java.util.List;
import java.util.UUID;

@Service
public interface LicenseService {
    ApplicationLicense createLicense(UUID productId,
                                     UUID ownerId,
                                     UUID licenseTypeId,
                                     Long count);

    ApplicationTicket activateLicense(String code,
                                      ApplicationDevice device,
                                      ApplicationUser user);

    String makeSignature(ApplicationTicket ticket);

    ApplicationTicket createTicket(ApplicationUser user,
                                   ApplicationDevice device,
                                   ApplicationLicense license,
                                   String info,
                                   String status);

    ApplicationTicket getActiveLicensesForDevice(ApplicationDevice device, String code);

    List<ApplicationLicense> getLicensesByProductId(UUID productId);

    ApplicationTicket renewLicense(String code, ApplicationUser user);
}
