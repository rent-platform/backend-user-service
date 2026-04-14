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
            regexp = "^\\+?[0-9]{11}$",
            message = "Phone must contain only digits and may start with +"
    )
    @Size(max = 11, message = "Phone must be at most 11 characters")
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