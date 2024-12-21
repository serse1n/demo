package ru.mtuci.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.demo.model.ApplicationUser;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<ApplicationUser, UUID> {
    void deleteByEmail(String email);
    ApplicationUser findByEmail(String email);
    Optional<ApplicationUser> findById(UUID id);
}
