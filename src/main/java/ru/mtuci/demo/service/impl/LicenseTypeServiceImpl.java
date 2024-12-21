package ru.mtuci.demo.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationLicenseType;
import ru.mtuci.demo.repository.LicenseTypeRepository;
import ru.mtuci.demo.service.LicenseTypeService;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class LicenseTypeServiceImpl implements LicenseTypeService {

    private LicenseTypeRepository licenseTypeRepository;

    @Override
    public ApplicationLicenseType saveApplicationLicenseType(ApplicationLicenseType applicationLicenseType) {
        return licenseTypeRepository.save(applicationLicenseType);
    }

    @Override
    public List<ApplicationLicenseType> getAllTypes() {
        return licenseTypeRepository.findAll();
    }

    @Override
    public ApplicationLicenseType getApplicationLicenseTypeByName(String name) {
        return licenseTypeRepository.findByName(name);
    }

    @Override
    public ApplicationLicenseType getLicenseTypeById(UUID id) {
        return licenseTypeRepository.findById(id)
                .orElse(null);
    }

    @Override
    public void deleteLicenseTypeByName(String name) {
        licenseTypeRepository.deleteByName(name);
    }
}
