package ru.mtuci.demo.service;

import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationLicense;
import ru.mtuci.demo.model.ApplicationLicenseHistory;
import ru.mtuci.demo.model.ApplicationUser;

import java.util.UUID;

@Service
public interface LicenseHistoryService {
    ApplicationLicenseHistory getLicenseHistoryById(UUID id);

    ApplicationLicenseHistory recordLicenseChange(ApplicationLicense license,
                                                  ApplicationUser owner,
                                                  String status);
}
