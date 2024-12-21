package ru.mtuci.demo.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationProduct;
import ru.mtuci.demo.repository.ProductRepository;
import ru.mtuci.demo.service.ProductService;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ApplicationProduct saveProduct(ApplicationProduct product) {
        return productRepository.save(product);
    }

    @Override
    public ApplicationProduct getProductById(UUID id) {
        return productRepository.findById(id)
                .orElse(null);
    }

    @Override
    public List<ApplicationProduct> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public void deleteProductById(UUID id) {
        productRepository.deleteById(id);
    }
}
