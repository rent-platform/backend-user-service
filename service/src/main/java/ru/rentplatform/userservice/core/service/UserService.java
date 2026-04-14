package ru.rentplatform.userservice.core.service;

import ru.rentplatform.userservice.api.dto.request.UpdateProfileRequest;
import ru.rentplatform.userservice.api.dto.response.MessageResponse;
import ru.rentplatform.userservice.api.dto.response.UserResponse;

import java.util.UUID;

public interface UserService {

    UserResponse getById(UUID id);

    UserResponse getCurrentUser(UUID userId);

    UserResponse updateCurrentUser(UUID userId, UpdateProfileRequest request);

    MessageResponse deleteCurrentUser(UUID userId);
}