package ru.rentplatform.userservice.core.service;

import ru.rentplatform.userservice.core.dao.entity.Session;

import java.util.UUID;

public interface SessionService {

    String createSession(UUID userId, String deviceInfo, boolean rememberMe);

    Session validateRefreshToken(String refreshToken);

    void revokeByRefreshToken(String refreshToken);

    void revokeAllUserSessions(UUID userId);
}
