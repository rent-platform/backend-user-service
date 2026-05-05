package ru.rentplatform.userservice.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.rentplatform.userservice.api.dto.request.*;
import ru.rentplatform.userservice.api.dto.response.*;
import ru.rentplatform.userservice.core.service.AuthService;

import static ru.rentplatform.userservice.api.ApiPaths.AUTH;

@RestController
@RequestMapping(AUTH)
@RequiredArgsConstructor
@Validated
@Tag(name = "Авторизация", description = "Регистрация, вход, обновление и выход")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Регистрация", description = "Создание нового аккаунта по номеру телефона и паролю")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Вход", description = "Аутентификация по логину (email или телефон) и паролю. " +
            "Возвращает access и refresh токены")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest) {
        String userAgent = httpServletRequest.getHeader("User-Agent");
        return ResponseEntity.ok(authService.login(request, userAgent));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Обновить токен", description = "Получить новый access-токен по действующему refresh-токену")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Выход", description = "Отзыв refresh-токена. Текущая сессия завершается")
    public ResponseEntity<MessageResponse> logout(
            @Valid @RequestBody LogoutRequest request) {
        return ResponseEntity.ok(authService.logout(request));
    }
}
