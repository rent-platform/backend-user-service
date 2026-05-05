package ru.rentplatform.userservice.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.rentplatform.userservice.api.dto.request.BillingProfileRequest;
import ru.rentplatform.userservice.api.dto.response.BillingProfileResponse;
import ru.rentplatform.userservice.core.service.BillingProfileService;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/me/billing")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Платёжный профиль", description = "Сохранённые платёжные данные пользователя в ЮKassa")
public class BillingProfileController {

    private final BillingProfileService billingProfileService;

    @GetMapping
    @Operation(summary = "Получить платёжный профиль",
            description = "Возвращает сохранённый платёжный профиль текущего пользователя")
    public BillingProfileResponse getBillingProfile(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return billingProfileService.getOrCreateBillingProfile(userId);
    }

    @PutMapping
    @Operation(summary = "Обновить платёжный профиль",
            description = "Внутренний эндпоинт для обновления из других сервисов")
    public void updateBillingProfile(@PathVariable UUID userId,
                                     @RequestBody BillingProfileRequest request) {
        billingProfileService.updateBillingProfile(
                userId,
                request.getCustomerId(),
                request.getPaymentMethodId()
        );
    }
}
