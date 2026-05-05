package ru.rentplatform.userservice.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.rentplatform.userservice.api.dto.response.UserPublicResponse;
import ru.rentplatform.userservice.core.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Публичный профиль", description = "Публичная информация о пользователе")
public class UserPublicController {

    private final UserService userService;

    @GetMapping("/{userId}/public")
    @Operation(summary = "Публичный профиль", description = "Возвращает никнейм, " +
            "аватар и общий рейтинг пользователя. Доступно без авторизации")
    public UserPublicResponse getPublicUser(
            @PathVariable UUID userId
    ) {
        return userService.getPublicProfile(userId);
    }
}
