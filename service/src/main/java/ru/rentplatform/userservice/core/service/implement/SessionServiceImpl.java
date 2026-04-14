package ru.rentplatform.userservice.core.service.implement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rentplatform.userservice.api.exception.SessionNotFoundException;
import ru.rentplatform.userservice.config.JwtProperties;
import ru.rentplatform.userservice.core.dao.entity.Session;
import ru.rentplatform.userservice.core.dao.repository.SessionRepository;
import ru.rentplatform.userservice.core.service.JwtService;
import ru.rentplatform.userservice.core.service.SessionService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public String createSession(UUID userId, String deviceInfo) {
        String refreshToken = jwtService.generateRefreshToken();
        String refreshTokenHash = hashToken(refreshToken);

        OffsetDateTime now = OffsetDateTime.now();

        Session session = new Session();
        session.setUserId(userId);
        session.setRefreshTokenHash(refreshTokenHash);
        session.setDeviceInfo(deviceInfo);
        session.setCreatedAt(now);
        session.setExpiresAt(now.plusSeconds(jwtProperties.getRefreshTokenExpirationSeconds()));

        sessionRepository.save(session);

        return refreshToken;
    }

    @Override
    @Transactional(readOnly = true)
    public Session validateRefreshToken(String refreshToken) {
        String hash = hashToken(refreshToken);

        Session session = sessionRepository.findByRefreshTokenHash(hash)
                .orElseThrow(() -> new SessionNotFoundException("Session not found"));

        if (session.getRevokedAt() != null ||
                session.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new SessionNotFoundException("Refresh token expired or revoked");
        }

        return session;
    }

    @Override
    @Transactional
    public void revokeByRefreshToken(String refreshToken) {
        String hash = hashToken(refreshToken);

        Session session = sessionRepository.findByRefreshTokenHash(hash)
                .orElseThrow(() -> new SessionNotFoundException("Session not found"));

        session.setRevokedAt(OffsetDateTime.now());
        sessionRepository.save(session);
    }

    @Override
    @Transactional
    public void revokeAllUserSessions(UUID userId) {
        List<Session> sessions = sessionRepository.findAllByUserIdAndRevokedAtIsNull(userId);
        OffsetDateTime now = OffsetDateTime.now();

        for (Session session : sessions) {
            session.setRevokedAt(now);
        }

        sessionRepository.saveAll(sessions);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to hash token", ex);
        }
    }
}
