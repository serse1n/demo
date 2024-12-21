package ru.mtuci.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.demo.model.ApplicationLicenseType;

import java.util.UUID;

public interface LicenseTypeRepository extends JpaRepository<ApplicationLicenseType, UUID> {
    ApplicationLicenseType findByName(String name);
    void deleteByName(String name);
}
