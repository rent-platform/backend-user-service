package ru.rentplatform.userservice.core.service.implement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rentplatform.userservice.api.dto.response.BillingProfileResponse;
import ru.rentplatform.userservice.core.dao.entity.UserBillingProfile;
import ru.rentplatform.userservice.core.dao.repository.UserBillingProfileRepository;
import ru.rentplatform.userservice.core.service.BillingProfileService;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillingProfileServiceImpl implements BillingProfileService {

    private final UserBillingProfileRepository billingProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public BillingProfileResponse getOrCreateBillingProfile(UUID userId) {
        UserBillingProfile profile = billingProfileRepository.findByUserId(userId)
                .orElseGet(() -> createEmptyProfile(userId));

        return BillingProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .customerId(profile.getCustomerId())
                .defaultPaymentMethodId(profile.getDefaultPaymentMethodId())
                .build();
    }

    @Override
    @Transactional
    public void updateBillingProfile(UUID userId, String customerId, String paymentMethodId) {
        UserBillingProfile profile = billingProfileRepository.findByUserId(userId)
                .orElseGet(() -> createEmptyProfile(userId));

        if (customerId != null) {
            profile.setCustomerId(customerId);
        }
        if (paymentMethodId != null) {
            profile.setDefaultPaymentMethodId(paymentMethodId);
        }
        profile.setUpdatedAt(OffsetDateTime.now());

        billingProfileRepository.save(profile);
    }

    private UserBillingProfile createEmptyProfile(UUID userId) {
        OffsetDateTime now = OffsetDateTime.now();
        UserBillingProfile profile = new UserBillingProfile();
        profile.setUserId(userId);
        profile.setCreatedAt(now);
        profile.setUpdatedAt(now);
        return billingProfileRepository.save(profile);
    }
}
