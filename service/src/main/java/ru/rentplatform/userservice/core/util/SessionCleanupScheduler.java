package ru.rentplatform.userservice.core.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.rentplatform.userservice.config.SessionCleanupProperties;
import ru.rentplatform.userservice.core.dao.repository.SessionRepository;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionCleanupScheduler {

    private final SessionRepository sessionRepository;
    private final SessionCleanupProperties properties;

    // каждые 30 минут
    @Scheduled(fixedDelayString = "${app.session.cleanup.delay-ms}")
    @Transactional
    public void cleanupSessions() {

        OffsetDateTime now = OffsetDateTime.now();

        int deletedExpired = sessionRepository.deleteAllExpired(now);

        OffsetDateTime threshold = now.minusDays(properties.getRevokedRetentionDays());
        int deletedRevoked = sessionRepository.deleteAllRevokedOlderThan(threshold);

        log.info("Session cleanup done: expired={}, revoked={}", deletedExpired, deletedRevoked);
    }
}
