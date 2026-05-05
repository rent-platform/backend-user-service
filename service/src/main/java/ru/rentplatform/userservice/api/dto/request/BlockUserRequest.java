package ru.rentplatform.userservice.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на блокировку/разблокировку пользователя")
public class BlockUserRequest {

    @NotBlank
    @Size(max = 1000)
    @Schema(description = "Причина блокировки", example = "Мошенничество")
    private String reason;
}
