package ru.mtuci.demo.service;

import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationDevice;
import ru.mtuci.demo.model.ApplicationDeviceLicense;
import ru.mtuci.demo.model.ApplicationLicense;

import java.util.List;
import java.util.UUID;

@Service
public interface DeviceLicenseService {
    ApplicationDeviceLicense getDeviceById(UUID id);

    Long getDeviceCount(UUID licenseId);

    List<ApplicationDeviceLicense> getAllLicensesById(ApplicationDevice device);

    List<ApplicationDeviceLicense> getDeviceLicensesByDeviceId(UUID id);

    void deleteById(UUID id);

    ApplicationDeviceLicense createDeviceLicense(ApplicationLicense license, ApplicationDevice device);
}
