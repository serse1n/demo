package ru.mtuci.demo.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public interface TokenService {

    List<String> issueTokenPair(
            String email,
            UUID deviceId, Set<GrantedAuthority> authorities);

    List<String> refreshTokenPair(
            UUID deviceId,
            String refreshToken);
}
