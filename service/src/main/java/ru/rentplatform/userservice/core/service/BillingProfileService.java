package ru.rentplatform.userservice.core.service;

import ru.rentplatform.userservice.api.dto.response.BillingProfileResponse;

import java.util.UUID;

public interface BillingProfileService {

    BillingProfileResponse getOrCreateBillingProfile(UUID userId);

    void updateBillingProfile(UUID userId, String customerId, String paymentMethodId);
}
