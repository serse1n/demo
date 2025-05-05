package ru.mtuci.demo.service;

import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationSignature;
import ru.mtuci.demo.repository.SignatureHistoryRepository;

@Service
public interface SignatureHistoryService {

    void createSignatureHistory(ApplicationSignature signature);
}
