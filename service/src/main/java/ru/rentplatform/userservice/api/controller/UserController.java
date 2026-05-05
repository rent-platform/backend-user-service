package ru.rentplatform.userservice.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.rentplatform.userservice.api.dto.request.ChangePasswordRequest;
import ru.rentplatform.userservice.api.dto.request.UpdateProfileRequest;
import ru.rentplatform.userservice.api.dto.response.MessageResponse;
import ru.rentplatform.userservice.api.dto.response.UserResponse;
import ru.rentplatform.userservice.core.service.UserService;

import java.util.UUID;

import static ru.rentplatform.userservice.api.ApiPaths.USERS;

@RestController
@RequestMapping(USERS)
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Пользователи", description = "Управление профилем пользователя")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Получить пользователя по ID", description = "Возвращает данные пользователя. Требуется авторизация")
    public UserResponse getById(@PathVariable UUID id) {
        return userService.getById(id);
    }

    @GetMapping("/me")
    @Operation(summary = "Мой профиль", description = "Возвращает профиль текущего авторизованного пользователя")
    public UserResponse getMe(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return userService.getCurrentUser(userId);
    }

    @PutMapping("/me")
    @Operation(summary = "Обновить профиль", description = "Изменение данных внутри профиля")
    public UserResponse updateMe(@AuthenticationPrincipal Jwt jwt,
                                 @Valid @RequestBody UpdateProfileRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return userService.updateCurrentUser(userId, request);
    }

    @DeleteMapping("/me")
    @Operation(summary = "Удалить аккаунт", description = "Мягкое удаление аккаунта. Все сессии завершаются")
    public MessageResponse deleteMe(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return userService.deleteCurrentUser(userId);
    }

    @PutMapping("/me/password")
    @Operation(summary = "Сменить пароль", description = "Смена пароля. Требуется текущий пароль. Все сессии завершаются")
    public MessageResponse changePassword(@AuthenticationPrincipal Jwt jwt,
                                          @Valid @RequestBody ChangePasswordRequest request){
        UUID  userId = UUID.fromString(jwt.getSubject());
        return userService.changePassword(userId, request);
    }

    @GetMapping("/test")
    @Operation(summary = "Тестовый эндпоинт", description = "Проверка работоспособности сервиса")
    @SecurityRequirement(name = "")
    public String test() {
        return "user-service route ok";
    }
}
