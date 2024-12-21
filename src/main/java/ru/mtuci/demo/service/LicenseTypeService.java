package ru.mtuci.demo.service;

import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationLicenseType;

import java.util.List;
import java.util.UUID;

@Service
public interface LicenseTypeService {
    ApplicationLicenseType saveApplicationLicenseType(ApplicationLicenseType applicationLicenseType);

    List<ApplicationLicenseType> getAllTypes();

    ApplicationLicenseType getApplicationLicenseTypeByName(String name);

    ApplicationLicenseType getLicenseTypeById(UUID id);

    void deleteLicenseTypeByName(String name);
}
