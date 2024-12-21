package ru.mtuci.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.demo.model.ApplicationLicense;

import java.util.List;
import java.util.UUID;

public interface LicenseRepository extends JpaRepository<ApplicationLicense, UUID> {
    ApplicationLicense findByCode(String code);
    ApplicationLicense findByIdInAndCode(List<UUID> ids, String code);
    List<ApplicationLicense> findAllByProductId(UUID productId);
}
