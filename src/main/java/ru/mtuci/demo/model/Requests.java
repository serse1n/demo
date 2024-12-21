package ru.mtuci.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

public class Requests {

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeleteUserRequest {
        String email;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeleteDeviceRequest {
        UUID id;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApplicationDeviceRequest {
        private String name;
        private String macAddress;
        private UUID userid;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LicenseCreateRequest {
        private UUID productId;
        private UUID ownerId;
        private UUID licenseTypeId;
        private Long count;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LicenseActivateRequest {
            String activationCode;
            String name;
            String mac_address;
            UUID deviceId;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LicenseInfoRequest {
        private String name;
        private String mac_address;
        private String activationCode;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LicenseRenewalRequest {
        private String activationCode;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LicenseTypeDeleteRequest {
        String name;
    }
}
