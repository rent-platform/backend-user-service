package ru.rentplatform.userservice.core.service.implement;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rentplatform.userservice.api.dto.request.*;
import ru.rentplatform.userservice.api.dto.response.*;
import ru.rentplatform.userservice.api.exception.*;
import ru.rentplatform.userservice.config.JwtProperties;
import ru.rentplatform.userservice.core.dao.entity.Session;
import ru.rentplatform.userservice.core.dao.entity.User;
import ru.rentplatform.userservice.core.dao.repository.SessionRepository;
import ru.rentplatform.userservice.core.dao.repository.UserRepository;
import ru.rentplatform.userservice.core.mapper.UserMapper;
import ru.rentplatform.userservice.core.service.AuthService;
import ru.rentplatform.userservice.core.service.JwtService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByPhoneAndDeletedAtIsNull(request.getPhone())) {
            throw new UserAlreadyExistsException("User with this phone already exists");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidCredentialsException("Passwords do not match");
        }

        OffsetDateTime now = OffsetDateTime.now();

        User user = new User();
        user.setNickname(request.getNickname());
        user.setFullName(request.getNickname());
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("user");
        user.setIsActive(true);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = findUserByLogin(request.getLogin());

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new InvalidCredentialsException("User account is inactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid login or password");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken();
        String refreshTokenHash = hashToken(refreshToken);

        OffsetDateTime now = OffsetDateTime.now();

        Session session = new Session();
        session.setUserId(user.getId());
        session.setRefreshTokenHash(refreshTokenHash);
        session.setDeviceInfo(request.getDeviceInfo());
        session.setCreatedAt(now);
        session.setExpiresAt(now.plusSeconds(jwtProperties.getRefreshTokenExpirationSeconds()));

        sessionRepository.save(session);

        user.setLastLoginAt(now);
        user.setUpdatedAt(now);
        userRepository.save(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationSeconds())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        String hash = hashToken(request.getRefreshToken());

        Session session = sessionRepository.findByRefreshTokenHash(hash)
                .orElseThrow(() -> new SessionNotFoundException("Session not found"));

        if (session.getRevokedAt() != null ||
                session.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new SessionNotFoundException("Refresh token expired or revoked");
        }

        User user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        String accessToken = jwtService.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(request.getRefreshToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationSeconds())
                .build();
    }

    @Override
    @Transactional
    public MessageResponse logout(LogoutRequest request) {
        String hash = hashToken(request.getRefreshToken());

        Session session = sessionRepository.findByRefreshTokenHash(hash)
                .orElseThrow(() -> new SessionNotFoundException("Session not found"));

        session.setRevokedAt(OffsetDateTime.now());
        sessionRepository.save(session);

        return MessageResponse.builder()
                .message("Logged out successfully")
                .build();
    }

    private User findUserByLogin(String login) {
        if (isEmail(login)) {
            return userRepository.findByEmailAndDeletedAtIsNull(login)
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid login or password"));
        }
        return userRepository.findByPhoneAndDeletedAtIsNull(login)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid login or password"));
    }

    private boolean isEmail(String value) {
        return value != null && value.contains("@");
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
