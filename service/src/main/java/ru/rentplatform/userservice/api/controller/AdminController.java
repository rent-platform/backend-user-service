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
import ru.rentplatform.userservice.api.dto.request.BlockUserRequest;
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
            description = "super_admin может назначать admin и moderator. Admin может назначать только moderator. " +
                    "Нельзя изменить роль самому себе или равному/старшему по рангу.")
    public UserResponse updateUserRole(@AuthenticationPrincipal Jwt jwt,
                                       @PathVariable UUID userId,
                                       @Valid @RequestBody UpdateRoleRequest request) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        String currentUserRole = jwt.getClaimAsString("role");
        return userService.updateUserRole(currentUserId, currentUserRole, userId, request.getRole());
    }

    @PutMapping("/users/{userId}/block")
    @PreAuthorize("hasAnyRole('super_admin', 'admin', 'moderator')")
    @Operation(summary = "Заблокировать пользователя",
            description = "super_admin может блокировать admin, moderator, user. admin — moderator, user. moderator — user")
    public UserResponse blockUser(@AuthenticationPrincipal Jwt jwt,
                                  @PathVariable UUID userId,
                                  @Valid @RequestBody BlockUserRequest request) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        String currentUserRole = jwt.getClaimAsString("role");
        return userService.blockUser(currentUserId, currentUserRole, userId, request.getReason());
    }

    @PutMapping("/users/{userId}/unblock")
    @PreAuthorize("hasAnyRole('super_admin', 'admin', 'moderator')")
    @Operation(summary = "Разблокировать пользователя",
            description = "Снимает блокировку с пользователя. Доступно тем же ролям, что и блокировка")
    public UserResponse unblockUser(@AuthenticationPrincipal Jwt jwt,
                                    @PathVariable UUID userId) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        String currentUserRole = jwt.getClaimAsString("role");
        return userService.unblockUser(currentUserId, currentUserRole, userId);
    }
}
