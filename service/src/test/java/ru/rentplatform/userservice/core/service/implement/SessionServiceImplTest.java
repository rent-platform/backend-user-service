package ru.rentplatform.userservice.core.service.implement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rentplatform.userservice.api.exception.SessionNotFoundException;
import ru.rentplatform.userservice.config.JwtProperties;
import ru.rentplatform.userservice.core.dao.entity.Session;
import ru.rentplatform.userservice.core.dao.repository.SessionRepository;
import ru.rentplatform.userservice.core.service.JwtService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private SessionServiceImpl sessionService;

    @BeforeEach
    void setUp() {

        lenient().when(jwtProperties.getRefreshTokenShortExpirationSeconds()).thenReturn(86400L);
        lenient().when(jwtProperties.getRefreshTokenRememberMeExpirationSeconds()).thenReturn(2592000L);
    }

    @Test
    void createSession_shouldCreateAndReturnToken() {

        UUID userId = UUID.randomUUID();
        when(jwtService.generateRefreshToken()).thenReturn("refresh-uuid-1.uuid-2");
        when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

        String token = sessionService.createSession(userId, "Chrome", false);

        assertNotNull(token);
        assertTrue(token.contains("."));
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void validateRefreshToken_shouldReturnSession_whenValid() {

        String token = "test-token";
        Session session = new Session();
        session.setUserId(UUID.randomUUID());
        session.setExpiresAt(OffsetDateTime.now().plusDays(1));

        when(sessionRepository.findByRefreshTokenHash(any())).thenReturn(Optional.of(session));

        Session result = sessionService.validateRefreshToken(token);

        assertNotNull(result);
    }

    @Test
    void validateRefreshToken_shouldThrow_whenNotFound() {

        when(sessionRepository.findByRefreshTokenHash(any())).thenReturn(Optional.empty());

        assertThrows(SessionNotFoundException.class, () ->
                sessionService.validateRefreshToken("invalid"));
    }

    @Test
    void validateRefreshToken_shouldThrow_whenRevoked() {

        Session session = new Session();
        session.setRevokedAt(OffsetDateTime.now());

        when(sessionRepository.findByRefreshTokenHash(any())).thenReturn(Optional.of(session));

        assertThrows(SessionNotFoundException.class, () ->
                sessionService.validateRefreshToken("revoked-token"));
    }

    @Test
    void validateRefreshToken_shouldThrow_whenExpired() {

        Session session = new Session();
        session.setExpiresAt(OffsetDateTime.now().minusDays(1));

        when(sessionRepository.findByRefreshTokenHash(any())).thenReturn(Optional.of(session));

        assertThrows(SessionNotFoundException.class, () ->
                sessionService.validateRefreshToken("expired-token"));
    }

    @Test
    void revokeByRefreshToken_shouldRevokeSession() {

        String token = "token-to-revoke";
        Session session = new Session();
        session.setExpiresAt(OffsetDateTime.now().plusDays(1));

        when(sessionRepository.findByRefreshTokenHash(any())).thenReturn(Optional.of(session));

        sessionService.revokeByRefreshToken(token);

        assertNotNull(session.getRevokedAt());
        verify(sessionRepository).save(session);
    }

    @Test
    void revokeAllUserSessions_shouldRevokeAll() {
        UUID userId = UUID.randomUUID();
        Session session1 = new Session();
        Session session2 = new Session();

        when(sessionRepository.findAllByUserIdAndRevokedAtIsNull(userId))
                .thenReturn(List.of(session1, session2));

        sessionService.revokeAllUserSessions(userId);

        assertNotNull(session1.getRevokedAt());
        assertNotNull(session2.getRevokedAt());
        verify(sessionRepository).saveAll(any());
    }
}
