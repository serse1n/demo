package ru.mtuci.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.demo.model.ApplicationDeviceLicense;

import java.util.List;
import java.util.UUID;

public interface DeviceLicenseRepository extends JpaRepository<ApplicationDeviceLicense, UUID> {
    Long countByLicenseId(UUID licenseId);
    List<ApplicationDeviceLicense> findByDeviceId(UUID deviceId);
}
