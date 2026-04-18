package ru.rentplatform.userservice.core.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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

    @Modifying
    @Query("DELETE FROM Session s WHERE s.expiresAt < :now")
    int deleteAllExpired(OffsetDateTime now);

    @Modifying
    @Query("DELETE FROM Session s WHERE s.revokedAt IS NOT NULL AND s.revokedAt < :threshold")
    int deleteAllRevokedOlderThan(OffsetDateTime threshold);
}
