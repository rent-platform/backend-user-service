package ru.rentplatform.userservice.core.service;

import ru.rentplatform.userservice.api.dto.request.LoginRequest;
import ru.rentplatform.userservice.api.dto.request.LogoutRequest;
import ru.rentplatform.userservice.api.dto.request.RefreshRequest;
import ru.rentplatform.userservice.api.dto.request.RegisterRequest;
import ru.rentplatform.userservice.api.dto.response.AuthResponse;
import ru.rentplatform.userservice.api.dto.response.MessageResponse;
import ru.rentplatform.userservice.api.dto.response.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshRequest request);

    MessageResponse logout(LogoutRequest request);
}
