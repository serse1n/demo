package ru.mtuci.demo.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationSignature;
import ru.mtuci.demo.model.SignatureStatus;
import ru.mtuci.demo.repository.SignatureAuditRepository;
import ru.mtuci.demo.repository.SignatureHistoryRepository;
import ru.mtuci.demo.repository.SignatureRepository;
import ru.mtuci.demo.service.SignatureAuditService;
import ru.mtuci.demo.service.SignatureHistoryService;
import ru.mtuci.demo.service.SignatureService;

import java.security.*;
import java.text.SimpleDateFormat;
import java.util.*;

import java.security.PrivateKey;
import java.security.PublicKey;

@Service
public class SignatureServiceImpl implements SignatureService {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private Date lastCheckTime;

    private final SignatureRepository signatureRepository;
    private final SignatureHistoryService signatureHistoryService;
    private final SignatureAuditService signatureAuditService;

    @Autowired
    public SignatureServiceImpl(SignatureRepository signatureRepository,
                                SignatureHistoryRepository signatureHistoryRepository,
                                SignatureAuditRepository signatureAuditRepository,
                                SignatureHistoryServiceImpl signatureHistoryService,
                                SignatureAuditServiceImpl signatureAuditService,
                                PrivateKey privateKey,
                                PublicKey publicKey) {
        this.signatureRepository = signatureRepository;
        this.signatureHistoryService = signatureHistoryService;
        this.signatureAuditService = signatureAuditService;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public List<ApplicationSignature> getAllActualSignatures(SignatureStatus status) {
        return signatureRepository.findByStatus(status);
    }

    public List<ApplicationSignature> getSignaturesUpdateAfter(String since) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            Date sinceDate = sdf.parse(since);

            Calendar calendar = Calendar.getInstance();

            calendar.setTime(sinceDate);
            calendar.add(Calendar.DATE, 1);

            Date nextDate = calendar.getTime();

            return signatureRepository.findByUpdatedAtAfter(nextDate);
        }
        catch (Exception e) {
            return null;
        }
    }

    public List<ApplicationSignature> getSignaturesByGuids(List<UUID> guids) {
        return signatureRepository.findByIdIn(guids);
    }

    public String makeHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(input.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }

    public String makeSignature(String res) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(res.getBytes());

            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            return "Something went wrong. The signature is not valid";
        }
    }

    @Scheduled(fixedRate = 5000)
    public void checkSignature() throws JsonProcessingException {
        List<ApplicationSignature> signatures = signatureRepository.findByUpdatedAtAfter(lastCheckTime);
        for (ApplicationSignature signature : signatures) {
            String originalDigitalSignature = signature.getDigitalSignature();
            signature.setDigitalSignature(null);
            ObjectMapper objectMapper = new ObjectMapper();

            String computedSignature = makeSignature(objectMapper.writeValueAsString(signature));
            if (!computedSignature.equals(originalDigitalSignature) && signature.getStatus() == SignatureStatus.ACTUAL) {
                signatureHistoryService.createSignatureHistory(signature);
                signature.setStatus(SignatureStatus.CORRUPTED);
                signatureRepository.save(signature);
                signatureAuditService.createSignatureAudit(signature.getId(), null,
                        "ERROR", new Date(), "Status");
            }
        }

        lastCheckTime = new Date();
    }

    public ApplicationSignature addSignature(String threatName,
                                             String firstBytes,
                                             Integer remainderLength,
                                             String fileType,
                                             Integer offsetStart,
                                             Integer offsetEnd,
                                             UUID userId) throws JsonProcessingException {

        String hash = makeHash(firstBytes.substring(firstBytes.length() - remainderLength * 2));

        ApplicationSignature signature = new ApplicationSignature();
        signature.setId(UUID.randomUUID());
        signature.setThreatName(threatName);
        signature.setFirstBytes(firstBytes);
        signature.setRemainderLength(remainderLength);
        signature.setFileType(fileType);
        signature.setOffsetStart(offsetStart);
        signature.setOffsetEnd(offsetEnd);
        signature.setRemainderHash(hash);

        Date date = new Date();
        signature.setUpdatedAt(date);
        signature.setStatus(SignatureStatus.ACTUAL);

        ObjectMapper objectMapper = new ObjectMapper();
        String digitalSignature = makeSignature(objectMapper.writeValueAsString(signature));
        signature.setDigitalSignature(digitalSignature);

        signatureRepository.save(signature);
        signatureHistoryService.createSignatureHistory(signature);
        signatureAuditService.createSignatureAudit(signature.getId(),
                userId,
                "CREATED",
                date,
                "ALL");

        return signature;
    }

    public String deleteSignature(UUID signatureUUID, UUID userId) {
        ApplicationSignature signature = signatureRepository.findById(signatureUUID)
                .orElse(null);

        if (signature == null) {
            return "Wrong UUID";
        }

        if (signature.getStatus().equals(SignatureStatus.DELETED)) {
            return "Signature already deleted";
        }

        signature.setStatus(SignatureStatus.DELETED);
        signatureRepository.save(signature);
        signatureAuditService.createSignatureAudit(signatureUUID,
                userId,
                "DELETED",
                new Date(),
                "Status");

        return "Signature has been successfully marked as deleted";
    }

    public ApplicationSignature updateSignature(UUID signatureUUID,
                                                String threatName,
                                                String firstBytes,
                                                Integer remainderLength,
                                                String fileType,
                                                Integer offsetStart,
                                                Integer offsetEnd,
                                                UUID userId,
                                                SignatureStatus status) throws JsonProcessingException {

        ApplicationSignature signature = signatureRepository.findById(signatureUUID)
                .orElse(null);

        if (signature == null) {
            return null;
        }

        List<String> changedFields = new ArrayList<>();

        signatureHistoryService.createSignatureHistory(signature);

        if (firstBytes != null) {
            signature.setFirstBytes(firstBytes);
            changedFields.add("firstBytes");
        }

        if (remainderLength != null) {
            signature.setRemainderLength(remainderLength);
            changedFields.add("remainderLength");
        }

        String hash = makeHash(signature.getFirstBytes()
                .substring(firstBytes.length() - signature.getRemainderLength() * 2));

        signature.setRemainderHash(hash);

        if (threatName != null) {
            signature.setThreatName(threatName);
            changedFields.add("threatName");
        }

        if (fileType != null) {
            signature.setFileType(fileType);
            changedFields.add("fileType");
        }

        if (offsetStart != null) {
            signature.setOffsetStart(offsetStart);
            changedFields.add("offsetStart");
        }

        if (offsetEnd != null) {
            signature.setOffsetEnd(offsetEnd);
            changedFields.add("offsetEnd");
        }

        if (status == SignatureStatus.ACTUAL ||
                status == SignatureStatus.DELETED ||
                status == SignatureStatus.CORRUPTED) {

            signature.setStatus(status);
            changedFields.add("status");
        }

        Date date = new Date();
        signature.setUpdatedAt(date);

        ObjectMapper objectMapper = new ObjectMapper();
        signature.setDigitalSignature(makeSignature(objectMapper.writeValueAsString(signature)));

        signatureRepository.save(signature);
        signatureAuditService.createSignatureAudit(signatureUUID,
                userId,
                "UPDATED",
                date,
                String.join(", ", changedFields));

        return signature;
    }
}
