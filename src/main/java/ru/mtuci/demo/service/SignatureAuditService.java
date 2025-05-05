package ru.mtuci.demo.service;

import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationSignatureAudit;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public interface SignatureAuditService {

    void createSignatureAudit(UUID signatureId, UUID userId, String changedType, Date changedAt, String fieldChanged);
    List<ApplicationSignatureAudit> getAllAuditRecords();
}
