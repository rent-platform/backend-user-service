package ru.rentplatform.userservice.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank
    @Size(max = 255)
    private String login;

    @NotBlank
    @Size(min = 8, max = 255)
    private String password;
}
