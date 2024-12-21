package ru.mtuci.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.demo.model.ApplicationLicenseHistory;

import java.util.UUID;

public interface LicenseHistoryRepository extends JpaRepository<ApplicationLicenseHistory, UUID> {

}
