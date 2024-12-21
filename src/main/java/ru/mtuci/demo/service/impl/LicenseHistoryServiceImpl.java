package ru.mtuci.demo.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationLicense;
import ru.mtuci.demo.model.ApplicationLicenseHistory;
import ru.mtuci.demo.model.ApplicationUser;
import ru.mtuci.demo.repository.LicenseHistoryRepository;
import ru.mtuci.demo.service.LicenseHistoryService;

import java.util.Date;
import java.util.UUID;

@Service
@AllArgsConstructor
public class LicenseHistoryServiceImpl implements LicenseHistoryService {

    private LicenseHistoryRepository licenseHistoryRepository;

    @Override
    public ApplicationLicenseHistory getLicenseHistoryById(UUID id) {
        return licenseHistoryRepository.findById(id)
                .orElse(null);
    }

    @Override
    public ApplicationLicenseHistory recordLicenseChange(ApplicationLicense license,
                                                         ApplicationUser owner,
                                                         String status)
    {
        ApplicationLicenseHistory newHistory = new ApplicationLicenseHistory();
        newHistory.setLicense(license);
        newHistory.setStatus(status);
        newHistory.setChangeDate(new Date());
        newHistory.setDescription(license.getDescription());
        newHistory.setUser(owner);

        return licenseHistoryRepository.save(newHistory);
    }
}
