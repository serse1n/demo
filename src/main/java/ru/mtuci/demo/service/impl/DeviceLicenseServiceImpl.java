package ru.mtuci.demo.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationDevice;
import ru.mtuci.demo.model.ApplicationDeviceLicense;
import ru.mtuci.demo.model.ApplicationLicense;
import ru.mtuci.demo.repository.DeviceLicenseRepository;
import ru.mtuci.demo.service.DeviceLicenseService;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class DeviceLicenseServiceImpl implements DeviceLicenseService {

    private final DeviceLicenseRepository deviceLicenseRepository;

    @Override
    public ApplicationDeviceLicense getDeviceById(UUID id) {
        return deviceLicenseRepository.findById(id)
                .orElse(null);
    }

    @Override
    public Long getDeviceCount(UUID licenseId) {
        return deviceLicenseRepository.countByLicenseId(licenseId);
    }

    @Override
    public List<ApplicationDeviceLicense> getAllLicensesById(ApplicationDevice device) {
        return deviceLicenseRepository.findByDeviceId(device.getId());
    }

    @Override
    public List<ApplicationDeviceLicense> getDeviceLicensesByDeviceId(UUID id) {
        return deviceLicenseRepository.findByDeviceId(id);
    }

    @Override
    public void deleteById(UUID id) {
        deviceLicenseRepository.deleteById(id);
    }

    @Override
    public ApplicationDeviceLicense createDeviceLicense(ApplicationLicense license, ApplicationDevice device) {
        ApplicationDeviceLicense newDeviceLicense = new ApplicationDeviceLicense();

        newDeviceLicense.setLicense(license);
        newDeviceLicense.setDevice(device);
        newDeviceLicense.setActivationDate(new Date());

        return deviceLicenseRepository.save(newDeviceLicense);
    }
}
