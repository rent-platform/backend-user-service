package ru.rentplatform.userservice.core.service.implement;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.rentplatform.userservice.api.dto.request.ChangePasswordRequest;
import ru.rentplatform.userservice.api.dto.request.UpdateProfileRequest;
import ru.rentplatform.userservice.api.dto.response.MessageResponse;
import ru.rentplatform.userservice.api.dto.response.UserPublicResponse;
import ru.rentplatform.userservice.api.dto.response.UserResponse;
import ru.rentplatform.userservice.api.exception.*;
import ru.rentplatform.userservice.client.DealPaymentClient;
import ru.rentplatform.userservice.client.audit.AuditClient;
import ru.rentplatform.userservice.core.dao.entity.User;
import ru.rentplatform.userservice.core.dao.repository.UserRepository;
import ru.rentplatform.userservice.core.mapper.UserMapper;
import ru.rentplatform.userservice.core.service.SessionService;
import ru.rentplatform.userservice.core.service.UserService;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final DealPaymentClient dealPaymentClient;
    private final AuditClient auditClient;

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

        if (request.getNickname() != null) {
            if (nickname != null) {
                boolean nicknameBusy = userRepository.existsByNicknameAndDeletedAtIsNullAndIdNot(nickname, userId);
                if (nicknameBusy) {
                    throw new NicknameAlreadyExistsException("User with this nickname already exists");
                }
            }
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

        Double rating = dealPaymentClient.getUserOverallRating(userId);

        return UserPublicResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .overallRating(rating)
                .build();
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(UUID currentUserId, String currentUserRole, UUID targetUserId, String newRole) {
        if (currentUserId.equals(targetUserId)) {
            throw new AccessDeniedException("Cannot change your own role");
        }

        User targetUser = userRepository.findByIdAndDeletedAtIsNull(targetUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String oldRole = targetUser.getRole();

        if (oldRole.equals(newRole)) {
            throw new IllegalArgumentException("User " + targetUser.getNickname() + " already has role: " + oldRole);
        }

        int currentRank = getRoleRank(currentUserRole);
        int targetRank = getRoleRank(oldRole);
        int newRank = getRoleRank(newRole);

        if (targetRank >= currentRank) {
            throw new AccessDeniedException("Cannot change role of user with equal or higher rank");
        }

        if (newRank >= currentRank) {
            throw new AccessDeniedException("Cannot assign role equal or higher than your own");
        }

        targetUser.setRole(newRole);
        targetUser.setUpdatedAt(OffsetDateTime.now());

        userRepository.save(targetUser);

        log.info("User {} role changed from {} to {} by {}", targetUserId, oldRole, newRole, currentUserId);

        auditClient.sendLog("user-service", currentUserId, "admin",
                "CHANGE_ROLE", "USER", targetUserId.toString(),
                "{\"newRole\": \"" + newRole + "\"}");

        return userMapper.toResponse(targetUser);
    }

    @Override
    @Transactional
    public UserResponse blockUser(UUID currentUserId, String currentUserRole, UUID targetUserId, String reason) {
        if (currentUserId.equals(targetUserId)) {
            throw new AccessDeniedException("Cannot block yourself");
        }

        User targetUser = userRepository.findByIdAndDeletedAtIsNull(targetUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!targetUser.getIsActive()) {
            throw new IllegalArgumentException("User is already blocked");
        }

        int currentRank = getRoleRank(currentUserRole);
        int targetRank = getRoleRank(targetUser.getRole());

        if (targetRank >= currentRank) {
            throw new AccessDeniedException("Cannot block user with equal or higher rank");
        }

        if ("super_admin".equals(targetUser.getRole())) {
            throw new AccessDeniedException("Cannot block super_admin");
        }

        OffsetDateTime now = OffsetDateTime.now();
        targetUser.setIsActive(false);
        targetUser.setBlockedAt(now);
        targetUser.setBlockedBy(currentUserId);
        targetUser.setBlockedReason(reason);
        targetUser.setUpdatedAt(now);

        userRepository.save(targetUser);

        sessionService.revokeAllUserSessions(targetUserId);

        log.info("User {} blocked by {} with reason: {}", targetUserId, currentUserId, reason);

        auditClient.sendLog("user-service", currentUserId, "moderator",
                "BLOCK_USER", "USER", targetUserId.toString(),
                "{\"reason\": \"" + reason + "\"}");

        return userMapper.toResponse(targetUser);
    }

    @Override
    @Transactional
    public UserResponse unblockUser(UUID currentUserId, String currentUserRole, UUID targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new AccessDeniedException("Cannot unblock yourself");
        }

        User targetUser = userRepository.findByIdAndDeletedAtIsNull(targetUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (targetUser.getIsActive()) {
            throw new IllegalArgumentException("User is not blocked");
        }

        int currentRank = getRoleRank(currentUserRole);
        int targetRank = getRoleRank(targetUser.getRole());

        if (targetRank >= currentRank) {
            throw new AccessDeniedException("Cannot unblock user with equal or higher rank");
        }

        targetUser.setIsActive(true);
        targetUser.setBlockedAt(null);
        targetUser.setBlockedBy(null);
        targetUser.setBlockedReason(null);
        targetUser.setUpdatedAt(OffsetDateTime.now());

        userRepository.save(targetUser);

        log.info("User {} unblocked by {}", targetUserId, currentUserId);

        auditClient.sendLog("user-service", currentUserId, "moderator",
                "UNBLOCK_USER", "USER", targetUserId.toString(), null);

        return userMapper.toResponse(targetUser);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private int getRoleRank(String role) {
        return switch (role) {
            case "super_admin" -> 4;
            case "admin" -> 3;
            case "moderator" -> 2;
            case "user" -> 1;
            default -> 0;
        };
    }
}
