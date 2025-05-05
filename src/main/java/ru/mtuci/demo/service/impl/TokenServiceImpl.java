package ru.mtuci.demo.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.configuration.JwtTokenProvider;
import ru.mtuci.demo.model.ApplicationUser;
import ru.mtuci.demo.model.SessionStatus;
import ru.mtuci.demo.model.UserSession;
import ru.mtuci.demo.repository.UserRepository;
import ru.mtuci.demo.repository.UserSessionRepository;
import ru.mtuci.demo.service.TokenService;

import java.util.*;

@Service
@AllArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserSessionRepository userSessionRepository;
    private final UserRepository userRepository;

    public List<String> issueTokenPair(
            String email,
            UUID deviceId, Set<GrantedAuthority> authorities) {

        String accessToken = jwtTokenProvider.createAccessToken(email, authorities);
        String refreshToken = jwtTokenProvider.createRefreshToken(email, deviceId);

        Long currentTimeMillis = System.currentTimeMillis();
        Date accessTokenExpire = new Date(currentTimeMillis + 5 * 60 * 1000);
        Date refreshTokenExpire = new Date(currentTimeMillis + 24 * 60 * 60 * 1000);

        UserSession userSession = new UserSession();
        userSession.setAccessToken(accessToken);
        userSession.setRefreshToken(refreshToken);
        userSession.setEmail(email);
        userSession.setAccessTokenExpiry(accessTokenExpire);
        userSession.setRefreshTokenExpiry(refreshTokenExpire);
        userSession.setDeviceId(deviceId);
        userSession.setStatus(SessionStatus.ACTIVE);

        userSessionRepository.save(userSession);

        return Arrays.asList(accessToken, refreshToken);
    }

    public List<String> refreshTokenPair(
            UUID deviceId,
            String refreshToken
    )
    {
        UserSession session = userSessionRepository.findByRefreshToken(refreshToken).orElse(null);
        if (session == null || session.getStatus() != SessionStatus.ACTIVE ||
                !Objects.equals(session.getDeviceId(), deviceId)) {
            blockAllSessionsForUser(session.getEmail());
            return null;
        }

        session.setStatus(SessionStatus.USED);
        userSessionRepository.save(session);

        ApplicationUser applicationUser = userRepository.findByEmail(session.getEmail());

        if (applicationUser == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return issueTokenPair(session.getEmail(), deviceId, applicationUser.getRole().getGrantedAuthorities());
    }

    public void blockAllSessionsForUser(String email) {
        List<UserSession> sessions = userSessionRepository.findAllByEmail(email);

        for (UserSession session : sessions) {
            if (session.getStatus() != SessionStatus.ACTIVE) {
                session.setStatus(SessionStatus.REVOKED);
                userSessionRepository.save(session);
            }
        }
    }
}
