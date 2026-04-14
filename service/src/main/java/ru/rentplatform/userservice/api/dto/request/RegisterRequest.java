package ru.rentplatform.userservice.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank
    @Pattern(
            regexp = "^\\+?[1-9][0-9]{10}$",
            message = "Phone must contain 11 digits, may start with +, and cannot start with 0"
    )
    private String phone;

    @NotBlank
    @Size(min = 8, max = 255)
    private String password;

    @NotBlank
    @Size(min = 8, max = 255)
    private String confirmPassword;

    @NotBlank
    @Size(min = 1, max = 50, message = "Nickname must be between 1 and 50 characters")
    @Pattern(
            regexp = "^[A-Za-z0-9_\\-]+$",
            message = "Nickname can contain only letters, digits, underscores and hyphens"
    )
    private String nickname;
}