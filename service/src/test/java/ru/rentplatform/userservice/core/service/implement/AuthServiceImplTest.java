package ru.rentplatform.userservice.core.service.implement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.rentplatform.userservice.api.dto.request.LoginRequest;
import ru.rentplatform.userservice.api.dto.request.RegisterRequest;
import ru.rentplatform.userservice.api.dto.response.AuthResponse;
import ru.rentplatform.userservice.api.dto.response.UserResponse;
import ru.rentplatform.userservice.api.exception.InvalidCredentialsException;
import ru.rentplatform.userservice.api.exception.UserAlreadyExistsException;
import ru.rentplatform.userservice.core.dao.entity.User;
import ru.rentplatform.userservice.core.dao.repository.UserRepository;
import ru.rentplatform.userservice.core.mapper.UserMapper;
import ru.rentplatform.userservice.core.service.JwtService;
import ru.rentplatform.userservice.core.service.SessionService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionService sessionService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_shouldCreateUser_whenValid() {

        RegisterRequest request = RegisterRequest.builder()
                .phone("+79990000001").password("pass123")
                .confirmPassword("pass123").nickname("testuser").build();

        when(userRepository.existsByPhoneAndDeletedAtIsNull(request.getPhone())).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(any(User.class))).thenReturn(UserResponse.builder().build());

        UserResponse result = authService.register(request);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrow_whenPhoneExists() {

        RegisterRequest request = RegisterRequest.builder()
                .phone("+79990000001").password("pass123")
                .confirmPassword("pass123").nickname("testuser").build();

        when(userRepository.existsByPhoneAndDeletedAtIsNull(request.getPhone())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () ->
                authService.register(request));
    }

    @Test
    void login_shouldReturnTokens_whenValid() {

        LoginRequest request = LoginRequest.builder()
                .login("+79990000001").password("pass123").build();

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setPhone("+79990000001");
        user.setPasswordHash("hashed");
        user.setIsActive(true);

        when(userRepository.findByPhoneAndDeletedAtIsNull("+79990000001"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass123", "hashed")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(sessionService.createSession(any(), any(), anyBoolean())).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(1200L);

        AuthResponse result = authService.login(request, null);

        assertNotNull(result);
        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
    }

    @Test
    void login_shouldThrow_whenInvalidPassword() {

        LoginRequest request = LoginRequest.builder()
                .login("+79990000001").password("wrong").build();

        User user = new User();
        user.setPasswordHash("hashed");
        user.setIsActive(true);

        when(userRepository.findByPhoneAndDeletedAtIsNull("+79990000001"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () ->
                authService.login(request, null));
    }
}