package ru.mtuci.demo.service;

import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationProduct;

import java.util.List;
import java.util.UUID;

@Service
public interface ProductService {
    ApplicationProduct saveProduct(ApplicationProduct product);
    ApplicationProduct getProductById(UUID id);
    List<ApplicationProduct> getAllProducts();

    void deleteProductById(UUID id);
}
