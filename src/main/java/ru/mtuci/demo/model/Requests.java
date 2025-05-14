package ru.mtuci.demo.model;

import lombok.*;

import java.util.List;
import java.util.UUID;

public class Requests {

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginRequest {
        String email;
        String password;
        String deviceId;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TokenRefreshRequest {
        String refreshToken;
        UUID deviceId;
    }

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
    public static class CreateDeviceRequest {
        String name;
        String macAddress;
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

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SignaturesAddRequest {
        private String threatName;
        private String firstBytes;
        private Integer remainderLength;
        private String fileType;
        private Integer offsetStart;
        private Integer offsetEnd;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SignaturesUpdateRequest {
        private UUID signatureId;
        private String threatName;
        private String firstBytes;
        private Integer remainderLength;
        private String fileType;
        private Integer offsetStart;
        private Integer offsetEnd;
        private SignatureStatus status;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SignaturesByGuidsRequest {
        private List<UUID> guids;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SignaturesDeleteRequest {
        private UUID signatureUUID;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SignaturesByStatusRequest {
        private SignatureStatus status;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SignaturesUpdatedAfterRequest {
        private String since;
    }
}
