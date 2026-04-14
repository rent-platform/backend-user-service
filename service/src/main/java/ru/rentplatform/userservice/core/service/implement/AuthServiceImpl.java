package ru.rentplatform.userservice.core.service.implement;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rentplatform.userservice.api.dto.request.*;
import ru.rentplatform.userservice.api.dto.response.*;
import ru.rentplatform.userservice.api.exception.*;
import ru.rentplatform.userservice.core.dao.entity.Session;
import ru.rentplatform.userservice.core.dao.entity.User;
import ru.rentplatform.userservice.core.dao.repository.UserRepository;
import ru.rentplatform.userservice.core.mapper.UserMapper;
import ru.rentplatform.userservice.core.service.AuthService;
import ru.rentplatform.userservice.core.service.JwtService;
import ru.rentplatform.userservice.core.service.SessionService;

import java.time.OffsetDateTime;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
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
    public AuthResponse login(LoginRequest request, String deviceInfo) {

        User user = findUserByLogin(request.getLogin());

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new InvalidCredentialsException("User account is inactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid login or password");
        }

        String normalizeDeviceInfo = normalizeDeviceInfo(deviceInfo);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = sessionService.createSession(user.getId(), normalizeDeviceInfo);

        OffsetDateTime now = OffsetDateTime.now();

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

        Session session = sessionService.validateRefreshToken(request.getRefreshToken());

        User user = userRepository.findByIdAndDeletedAtIsNull(session.getUserId())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new InvalidCredentialsException("User account is inactive");
        }

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

        sessionService.revokeByRefreshToken(request.getRefreshToken());
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

    private String normalizeDeviceInfo(String deviceInfo) {
        if (deviceInfo == null) {
            return null;
        }

        String trimmed = deviceInfo.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed.length() > 255 ? trimmed.substring(0, 255) : trimmed;
    }

}
