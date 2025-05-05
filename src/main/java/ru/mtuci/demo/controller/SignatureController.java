package ru.mtuci.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.demo.configuration.JwtTokenProvider;
import ru.mtuci.demo.model.*;
import ru.mtuci.demo.service.SignatureAuditService;
import ru.mtuci.demo.service.SignatureService;
import ru.mtuci.demo.service.UserService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static ch.qos.logback.core.encoder.ByteArrayUtil.hexStringToByteArray;

@RestController
@RequestMapping("/signatures")
@RequiredArgsConstructor
public class SignatureController {

    private final SignatureService signatureService;
    private final SignatureAuditService signatureAuditService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @GetMapping("/actual")
    public ResponseEntity<?> getAllActualSignatures() {
        try{
            List<ApplicationSignature> signatures = signatureService.getAllActualSignatures(SignatureStatus.ACTUAL);

            return ResponseEntity.ok(signatures);
        }
        catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/updated-after")
    public ResponseEntity<?> getSignaturesUpdatedAfter(@RequestBody Requests.SignaturesUpdatedAfterRequest since) {
        try {
            List<ApplicationSignature> signatures = signatureService.getSignaturesUpdateAfter(since.getSince());

            return ResponseEntity.ok(Objects.requireNonNullElse(signatures, "Invalid date"));
        }
        catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/by-guids")
    public ResponseEntity<?> getSignaturesByGuids(@RequestBody Requests.SignaturesByGuidsRequest guids) {
        try {
            List<ApplicationSignature> signatures = signatureService.getSignaturesByGuids(guids.getGuids());

            return ResponseEntity.ok(signatures);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> addSignature(@RequestBody Requests.SignaturesAddRequest request, HttpServletRequest req) {
        try {
            String email = jwtTokenProvider.getUsername(req.getHeader("Authorization").substring(7));
            ApplicationUser user = userService.findUserByEmail(email);

            ApplicationSignature signature = signatureService.addSignature(request.getThreatName(), request.getFirstBytes(),
                    request.getRemainderLength(), request.getFileType(), request.getOffsetStart(),
                    request.getOffsetEnd(), user.getId());

            return ResponseEntity.ok(signature);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Oops, something went wrong....");
        }
    }

    @PostMapping("/delete")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> deleteSignature(@RequestBody Requests.SignaturesDeleteRequest signatureUUID, HttpServletRequest req) {
        try {
            String email = jwtTokenProvider.getUsername(req.getHeader("Authorization").substring(7));
            ApplicationUser user = userService.findUserByEmail(email);

            String result = signatureService.deleteSignature(signatureUUID.getSignatureUUID(), user.getId());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/update")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> updateSignature(@RequestBody Requests.SignaturesUpdateRequest request, HttpServletRequest req) {
        try {
            String email = jwtTokenProvider.getUsername(req.getHeader("Authorization").substring(7));
            ApplicationUser user = userService.findUserByEmail(email);

            ApplicationSignature signature = signatureService.updateSignature(request.getSignatureId(),
                    request.getThreatName(), request.getFirstBytes(), request.getRemainderLength(),
                    request.getFileType(), request.getOffsetStart(), request.getOffsetEnd(), user.getId(), request.getStatus());

            return ResponseEntity.ok(Objects.requireNonNullElse(signature, "Signature not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/by-status")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> getSignaturesByStatus(@RequestBody Requests.SignaturesByStatusRequest status) {
        try {
            List<ApplicationSignature> signatures = signatureService.getAllActualSignatures(status.getStatus());

            return ResponseEntity.ok(signatures);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/audit")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> signatureAudit() {
        try {
            List<ApplicationSignatureAudit> auditRecords = signatureAuditService.getAllAuditRecords();

            return ResponseEntity.ok(auditRecords);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @GetMapping(value = "/data")
    public ResponseEntity<?> getSignatureData() {
        try{
            List<ApplicationSignature> signatures = signatureService.getAllActualSignatures(SignatureStatus.ACTUAL);

            int signatureCount = signatures.size();

            ByteArrayOutputStream dataOutputStream = new ByteArrayOutputStream();
            writeLongBE(dataOutputStream, signatureCount);
            for (ApplicationSignature signature : signatures) {
                writeSignatureDataToStream(dataOutputStream, signature);
            }

            byte[] data = dataOutputStream.toByteArray();

            return ResponseEntity.ok(data);
        }
        catch (IOException ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/manifest", produces = MediaType.MULTIPART_MIXED_VALUE)
    public ResponseEntity<MultiValueMap<String, Object>> getSignatureManifest() {
        try{
            List<ApplicationSignature> signatures = signatureService.getAllActualSignatures(SignatureStatus.ACTUAL);

            int signatureCount = signatures.size();
            List<String> signatureEntries = new ArrayList<>();
            ByteArrayOutputStream dataOutputStream = new ByteArrayOutputStream();

            for (ApplicationSignature signature : signatures) {
                String entry = signature.getId() + ":" + signature.getDigitalSignature();
                signatureEntries.add(entry);

                writeSignatureDataToStream(dataOutputStream, signature);
            }

            byte[] manifest = createManifest(signatureCount, signatureEntries);
            byte[] data = dataOutputStream.toByteArray();

            return buildMultipartResponse(manifest, data);
        }
        catch (IOException ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<MultiValueMap<String, Object>> buildMultipartResponse(byte [] manifest, byte[] data) {
        ByteArrayResource manifestRes = new ByteArrayResource(manifest) {
            @Override
            public String getFilename() {
                return "manifest.bin";
            }
        };

        ByteArrayResource dataRes = new ByteArrayResource(data) {
            @Override
            public String getFilename() {
                return "data.bin";
            }
        };

        LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("manifest", new HttpEntity<>(manifestRes, createHeaders("manifest.bin")));
        parts.add("data", new HttpEntity<>(dataRes, createHeaders("data.bin")));

        return ResponseEntity.ok().contentType(MediaType.parseMediaType("multipart/mixed")).body(parts);
    }

    private HttpHeaders createHeaders(String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return headers;
    }

    private void writeUuidBE(ByteArrayOutputStream baos, UUID uuid) {
        writeLongBE(baos, uuid.getMostSignificantBits());
        writeLongBE(baos, uuid.getLeastSignificantBits());
    }

    private void writeLongBE(ByteArrayOutputStream baos, long value) {
        baos.write((byte) (value & 0xFF));
        baos.write((byte) (value >> 8 & 0xFF));
        baos.write((byte) (value >> 16 & 0xFF));
        baos.write((byte) (value >> 24 & 0xFF));
        baos.write((byte) (value >> 32 & 0xFF));
        baos.write((byte) (value >> 40 & 0xFF));
        baos.write((byte) (value >> 48 & 0xFF));
        baos.write((byte) (value >> 56 & 0xFF));
    }

    private void writeIntBE(ByteArrayOutputStream baos, int value) {
        baos.write((byte) (value & 0xFF));
        baos.write((byte) (value >> 8 & 0xFF));
        baos.write((byte) (value >> 16 & 0xFF));
        baos.write((byte) (value >> 24 & 0xFF));
    }

    private void writeStringBE(ByteArrayOutputStream baos, String value, boolean lenNeed) {
        byte[] bytes = value.getBytes();
        if (lenNeed) {
            writeLongBE(baos, bytes.length);
        }
        baos.writeBytes(bytes);
    }

    private void writeSignatureDataToStream(ByteArrayOutputStream baos, ApplicationSignature signature) throws IOException {
        writeUuidBE(baos, signature.getId());

        writeStringBE(baos, signature.getThreatName(), true);

        byte[] hexFirstBytes = hexStringToByteArray(signature.getFirstBytes());
        baos.write(hexFirstBytes);

        byte[] hexHashBytes = hexStringToByteArray(signature.getRemainderHash());
        baos.write(hexHashBytes);

        writeIntBE(baos, signature.getRemainderLength());

        writeStringBE(baos, signature.getFileType(), true);

        writeLongBE(baos, signature.getOffsetStart());

        writeLongBE(baos, signature.getOffsetEnd());
    }


    private byte[] createManifest(int signatureCount, List<String> signatureEntries) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        writeIntBE(baos, signatureCount);

        for (String entry : signatureEntries) {
            writeStringBE(baos, entry, true);
        }

        String manifestHash = signatureService.makeHash(baos.toString(StandardCharsets.UTF_8));
        writeStringBE(baos, signatureService.makeSignature(manifestHash), true);

        return baos.toByteArray();
    }
}
