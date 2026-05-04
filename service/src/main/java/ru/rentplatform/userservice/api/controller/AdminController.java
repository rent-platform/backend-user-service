package ru.rentplatform.userservice.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.rentplatform.userservice.api.dto.request.UpdateRoleRequest;
import ru.rentplatform.userservice.api.dto.response.UserResponse;
import ru.rentplatform.userservice.core.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Администрирование", description = "Управление ролями пользователей")
public class AdminController {

    private final UserService userService;

    @PutMapping("/users/{userId}/role")
    @PreAuthorize("hasAnyRole('super_admin', 'admin')")
    @Operation(summary = "Назначить роль пользователю",
            description = "super_admin может назначать admin и moderator. Admin может назначать только moderator. Нельзя изменить роль самому себе.")
    public UserResponse updateUserRole(@AuthenticationPrincipal Jwt jwt,
                                       @PathVariable UUID userId,
                                       @Valid @RequestBody UpdateRoleRequest request) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        String currentUserRole = jwt.getClaimAsString("role");
        return userService.updateUserRole(currentUserId, currentUserRole, userId, request.getRole());
    }
}
