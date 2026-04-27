package ru.rentplatform.userservice.core.service.implement;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.rentplatform.userservice.api.dto.request.ChangePasswordRequest;
import ru.rentplatform.userservice.api.dto.request.UpdateProfileRequest;
import ru.rentplatform.userservice.api.dto.response.MessageResponse;
import ru.rentplatform.userservice.api.dto.response.UserPublicResponse;
import ru.rentplatform.userservice.api.dto.response.UserResponse;
import ru.rentplatform.userservice.api.exception.AccessDeniedException;
import ru.rentplatform.userservice.api.exception.EmailAlreadyExistsException;
import ru.rentplatform.userservice.api.exception.InvalidCredentialsException;
import ru.rentplatform.userservice.api.exception.UserNotFoundException;
import ru.rentplatform.userservice.core.dao.entity.User;
import ru.rentplatform.userservice.core.dao.repository.UserRepository;
import ru.rentplatform.userservice.core.mapper.UserMapper;
import ru.rentplatform.userservice.core.service.SessionService;
import ru.rentplatform.userservice.core.service.UserService;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserResponse getById(UUID id) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AccessDeniedException("User not found or access denied"));

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new AccessDeniedException("Current user not found"));

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateCurrentUser(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new AccessDeniedException("Current user not found"));

        String fullName = normalize(request.getFullName());
        String nickname = normalize(request.getNickname());
        String email = normalize(request.getEmail());
        String bio = normalize(request.getBio());
        String avatarUrl = normalize(request.getAvatarUrl());

        if (request.getEmail() != null) {
            if (email != null) {
                boolean emailBusy = userRepository.existsByEmailAndDeletedAtIsNullAndIdNot(email, userId);
                if (emailBusy) {
                    throw new EmailAlreadyExistsException("User with this email already exists");
                }
            }
            user.setEmail(email);
        }

        if (fullName != null) {
            user.setFullName(fullName);
        }

        if (nickname != null) {
            user.setNickname(nickname);
        }

        user.setBio(bio);
        user.setAvatarUrl(avatarUrl);
        user.setUpdatedAt(OffsetDateTime.now());

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public MessageResponse deleteCurrentUser(UUID userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new AccessDeniedException("Current user not found"));

        OffsetDateTime now = OffsetDateTime.now();

        user.setIsActive(false);
        user.setDeletedAt(now);
        user.setUpdatedAt(now);
        userRepository.save(user);

        sessionService.revokeAllUserSessions(userId);

        return new MessageResponse("User deleted successfully");
    }

    @Override
    @Transactional
    public MessageResponse changePassword(UUID userId, ChangePasswordRequest request) {

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new InvalidCredentialsException("Passwords do not match");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("New password must be different from current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);

        sessionService.revokeAllUserSessions(userId);

        return new MessageResponse("Password changed successfully. Please login again");
    }


    @Override
    @Transactional(readOnly = true)
    public UserPublicResponse getPublicProfile(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new UserNotFoundException("User not found")
                );

        return UserPublicResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .rating(0.0)
                .build();
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
