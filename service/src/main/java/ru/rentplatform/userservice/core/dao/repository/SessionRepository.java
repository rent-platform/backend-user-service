package ru.rentplatform.userservice.core.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentplatform.userservice.core.dao.entity.Session;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    Optional<Session> findByRefreshTokenHash(String refreshTokenHash);

    List<Session> findAllByExpiresAtBefore(OffsetDateTime expiresAt);

    List<Session> findAllByRevokedAtBefore(OffsetDateTime revokedAt);

    List<Session> findAllByUserId(UUID userId);

    List<Session> findAllByUserIdAndRevokedAtIsNull(UUID userId);
}
