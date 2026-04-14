package ru.rentplatform.userservice.core.service.implement;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.rentplatform.userservice.api.dto.request.UpdateProfileRequest;
import ru.rentplatform.userservice.api.dto.response.MessageResponse;
import ru.rentplatform.userservice.api.dto.response.UserResponse;
import ru.rentplatform.userservice.api.exception.AccessDeniedException;
import ru.rentplatform.userservice.api.exception.EmailAlreadyExistsException;
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


    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
