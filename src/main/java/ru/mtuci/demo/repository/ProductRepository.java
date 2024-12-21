package ru.mtuci.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.demo.model.ApplicationProduct;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<ApplicationProduct, UUID> {
    Optional<ApplicationProduct> findById(UUID id);
}
