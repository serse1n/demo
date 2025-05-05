package ru.mtuci.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationSignature;
import ru.mtuci.demo.model.SignatureStatus;
import ru.mtuci.demo.repository.SignatureAuditRepository;
import ru.mtuci.demo.repository.SignatureHistoryRepository;
import ru.mtuci.demo.repository.SignatureRepository;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public interface SignatureService {

    List<ApplicationSignature> getAllActualSignatures(SignatureStatus status);

    List<ApplicationSignature> getSignaturesByGuids(List<UUID> guids);

    List<ApplicationSignature> getSignaturesUpdateAfter(String since);

    String makeHash(String input);

    String makeSignature(String res);

    void checkSignature() throws JsonProcessingException;

    ApplicationSignature addSignature(String threatName,
                                      String firstBytes,
                                      Integer remainderLength,
                                      String fileType,
                                      Integer offsetStart,
                                      Integer offsetEnd,
                                      UUID userId) throws JsonProcessingException;

    String deleteSignature(UUID signatureUUID, UUID userId);

    ApplicationSignature updateSignature(UUID signatureUUID,
                                         String threatName,
                                         String firstBytes,
                                         Integer remainderLength,
                                         String fileType,
                                         Integer offsetStart,
                                         Integer offsetEnd,
                                         UUID userId,
                                         SignatureStatus status) throws JsonProcessingException;
}
