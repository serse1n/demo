package ru.mtuci.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.demo.model.ApplicationDevice;

import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<ApplicationDevice, UUID> {
    Optional<ApplicationDevice> findById(UUID id);
    ApplicationDevice findByMacAddress(String macAddress);
}
