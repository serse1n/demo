package ru.mtuci.demo.controller;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.demo.model.ApplicationLicense;
import ru.mtuci.demo.model.ApplicationProduct;
import ru.mtuci.demo.service.LicenseService;
import ru.mtuci.demo.service.ProductService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@AllArgsConstructor
public class ProductController {

    private ProductService productService;
    private LicenseService licenseService;

    @GetMapping
    public ResponseEntity<?> getAllProducts(@RequestParam(value = "id", required = false) UUID id) {
        try {
            if (id != null) {
                ApplicationProduct product = productService.getProductById(id);

                if (product == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Product not found");
                }

                return ResponseEntity.ok(product);
            }

            return ResponseEntity.ok(productService.getAllProducts());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> createProduct(@RequestBody ApplicationProduct product) {
        try {
            product.setId(UUID.randomUUID());

            ApplicationProduct savedProduct = productService.saveProduct(product);

            return ResponseEntity.ok().body(savedProduct.getId());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> updateProduct(@RequestBody ApplicationProduct product) {
        try {
            ApplicationProduct updatedProduct = productService.getProductById(product.getId());

            if (updatedProduct == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Product not found");
            }

            updatedProduct.setName(product.getName());
            updatedProduct.setBlocked(product.isBlocked());

            productService.saveProduct(updatedProduct);

            return ResponseEntity.ok().body(updatedProduct.getId());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyAuthority('modification')")
    @Transactional
    public ResponseEntity<?> deleteProduct(@RequestBody ApplicationProduct product) {
        try {

            ApplicationProduct deletedProduct = productService.getProductById(product.getId());

            if (deletedProduct == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Product not found");
            }

            List<ApplicationLicense> licenses = licenseService.getLicensesByProductId(product.getId());

            if (!licenses.isEmpty()) {
                for (ApplicationLicense license : licenses) {
                    license.setProduct(null);
                }
            }

            productService.deleteProductById(deletedProduct.getId());

            return ResponseEntity.ok().body("Product deleted successfully");
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }
}
