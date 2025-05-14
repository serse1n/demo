package ru.mtuci.demo.controller;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.demo.model.ApplicationLicense;
import ru.mtuci.demo.model.ApplicationLicenseType;
import ru.mtuci.demo.model.Requests;
import ru.mtuci.demo.service.LicenseTypeService;

import java.util.List;

@RestController
@RequestMapping("/licenses/types")
@AllArgsConstructor
public class LicenseTypeController {

    private LicenseTypeService licenseTypeService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> findLicenseType(@RequestParam(value = "name", required = false) String name) {
        try {
            if (name == null) {
                List<ApplicationLicenseType> applicationLicenseType = licenseTypeService.getAllTypes();
                return ResponseEntity.ok(applicationLicenseType);
            }

            else {
                ApplicationLicenseType applicationLicenseType = licenseTypeService.getApplicationLicenseTypeByName(name);

                if (applicationLicenseType == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Application License Type not found");
                }
                return ResponseEntity.ok(applicationLicenseType);
            }
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> createLicenseType(@RequestBody ApplicationLicenseType licenseType) {
        try {
            if (licenseTypeService.getApplicationLicenseTypeByName(licenseType.getName()) != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("License type already exists");
            }

            ApplicationLicenseType createdLicense = licenseTypeService.saveApplicationLicenseType(licenseType);

            return ResponseEntity.ok(createdLicense.getId());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> updateLicenseType(@RequestBody ApplicationLicenseType licenseType) {
        try {
            ApplicationLicenseType updatedLicenseType = licenseTypeService.getApplicationLicenseTypeByName(licenseType.getName());

            if (updatedLicenseType == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("License type does not exist");
            }

            updatedLicenseType.setDefaultDuration(licenseType.getDefaultDuration());
            updatedLicenseType.setDescription(licenseType.getDescription());

            licenseTypeService.saveApplicationLicenseType(updatedLicenseType);

            return ResponseEntity.ok(updatedLicenseType);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyAuthority('modification')")
    @Transactional
    public ResponseEntity<?> deleteLicenseType(@RequestBody Requests.LicenseTypeDeleteRequest deleteRequest) {
        try {
            ApplicationLicenseType applicationLicenseType = licenseTypeService.getApplicationLicenseTypeByName(deleteRequest.getName());

            if (applicationLicenseType == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("License type does not exist");
            }

            licenseTypeService.deleteLicenseTypeByName(applicationLicenseType.getName());

            return ResponseEntity.ok("License type deleted");
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }
}
