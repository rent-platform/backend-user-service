package ru.rentplatform.userservice.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Запрос на изменение роли пользователя")
public class UpdateRoleRequest {

    @NotBlank
    @Pattern(regexp = "user|moderator|admin|super_admin", message = "Role must be 'user', 'moderator', 'admin' or 'super_admin'")
    @Schema(description = "Новая роль", example = "moderator", allowableValues = {"user", "moderator", "admin", "super_admin"})
    private String role;
}
