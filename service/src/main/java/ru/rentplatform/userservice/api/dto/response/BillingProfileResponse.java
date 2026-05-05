package ru.rentplatform.userservice.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Платёжный профиль пользователя")
public class BillingProfileResponse {

    @Schema(description = "ID профиля")
    private UUID id;

    @Schema(description = "ID пользователя")
    private UUID userId;

    @Schema(description = "ID плательщика в ЮKassa", example = "cus_abc123")
    private String customerId;

    @Schema(description = "ID сохранённого способа оплаты", example = "pm_card_abc")
    private String defaultPaymentMethodId;
}
