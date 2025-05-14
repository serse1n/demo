package ru.mtuci.demo.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.demo.configuration.JwtTokenProvider;
import ru.mtuci.demo.model.*;
import ru.mtuci.demo.service.*;

import java.util.UUID;

@RestController
@RequestMapping("/licenses")
@AllArgsConstructor
public class LicenseController {

    private final LicenseService licenseService;
    private final ProductService productService;
    private final UserService userService;
    private final LicenseTypeService licenseTypeService;
    private final JwtTokenProvider jwtTokenProvider;
    private final DeviceService deviceService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> createLicense(@RequestBody Requests.LicenseCreateRequest request,
                                           @RequestHeader("Authorization") String authHeader) {
        try {
            UUID productId = request.getProductId();
            UUID ownerId = request.getOwnerId();
            UUID licenseTypeId = request.getLicenseTypeId();

            if (productService.getProductById(productId) == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Product not found");
            }

            if (productService.getProductById(productId).isBlocked()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Product is blocked");
            }

            if (userService.findUserById(ownerId) == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("User not found");
            }

            if (licenseTypeService.getLicenseTypeById(licenseTypeId) == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("License type not found");
            }

            authHeader = authHeader.replace("Bearer ", "");

            String email = jwtTokenProvider.getUsername(authHeader);

            ApplicationLicense license = licenseService.createLicense(productId, ownerId, licenseTypeId, request.getCount());

            return ResponseEntity.ok(license.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Something went wrong");
        }
    }

    @PostMapping("/activate")
    public ResponseEntity<?> activateLicense(@RequestBody Requests.LicenseActivateRequest request,
                                             @RequestHeader("Authorization") String authHeader) {
        try {
            authHeader = authHeader.replace("Bearer ", "");

            String email = jwtTokenProvider.getUsername(authHeader);
            ApplicationUser user = userService.findUserByEmail(email);
            ApplicationDevice device = deviceService.getDeviceByMacAddress(request.getMac_address());

            ApplicationTicket ticket = licenseService.activateLicense(request.getActivationCode(), device, user);

            if (!ticket.getStatus().equals("OK"))
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Failed to activate license");
            }

            return ResponseEntity.ok(ticket);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }

    @PostMapping("/info")
    public ResponseEntity<?> infoLicense(@RequestBody Requests.LicenseInfoRequest request,
                                         @RequestHeader("Authorization") String authHeader) {
        try {
            authHeader = authHeader.replace("Bearer ", "");

            String email = jwtTokenProvider.getUsername(authHeader);
            ApplicationUser user = userService.findUserByEmail(email);
            ApplicationDevice device = deviceService.getDeviceByMacAddress(request.getMac_address());

            if (device == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Device not found");
            }

            ApplicationTicket ticket = licenseService.getActiveLicensesForDevice(device, request.getActivationCode());

            if (!ticket.getStatus().equals("OK")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ticket.getInfo());
            }

            return ResponseEntity.ok(ticket);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }

    @PostMapping("/renew")
    public ResponseEntity<?> renewLicense(@RequestBody Requests.LicenseRenewalRequest request,
                                            @RequestHeader("Authorization") String authHeader) {
        try {
            authHeader = authHeader.replace("Bearer ", "");

            String email = jwtTokenProvider.getUsername(authHeader);
            ApplicationUser user = userService.findUserByEmail(email);

            ApplicationTicket ticket = licenseService.renewLicense(request.getActivationCode(), user);

            if (!ticket.getStatus().equals("OK")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ticket.getInfo());
            }

            return ResponseEntity.ok(ticket);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }
}
