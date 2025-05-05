package ru.mtuci.demo.service.impl;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import ru.mtuci.demo.service.KeyPairService;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

@Component
public class KeyPairServiceImpl implements KeyPairService {

    @Bean
    public KeyPair keyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    @Bean
    public PrivateKey privateKey(KeyPair keyPair) {
        return keyPair.getPrivate();
    }

    @Bean
    public PublicKey publicKey(KeyPair keyPair) {
        return keyPair.getPublic();
    }
}
