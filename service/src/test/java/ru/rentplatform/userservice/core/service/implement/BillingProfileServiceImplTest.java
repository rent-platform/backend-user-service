package ru.rentplatform.userservice.core.service.implement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rentplatform.userservice.api.dto.response.BillingProfileResponse;
import ru.rentplatform.userservice.core.dao.entity.UserBillingProfile;
import ru.rentplatform.userservice.core.dao.repository.UserBillingProfileRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingProfileServiceImplTest {

    @Mock
    private UserBillingProfileRepository billingProfileRepository;

    @InjectMocks
    private BillingProfileServiceImpl billingProfileService;

    @Test
    void getOrCreateBillingProfile_shouldReturnExisting() {

        UUID userId = UUID.randomUUID();
        UserBillingProfile profile = new UserBillingProfile();
        profile.setId(UUID.randomUUID());
        profile.setUserId(userId);
        profile.setCustomerId("cus_123");
        profile.setDefaultPaymentMethodId("pm_456");

        when(billingProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        BillingProfileResponse result = billingProfileService.getOrCreateBillingProfile(userId);

        assertNotNull(result);
        assertEquals("cus_123", result.getCustomerId());
        assertEquals("pm_456", result.getDefaultPaymentMethodId());
    }

    @Test
    void getOrCreateBillingProfile_shouldCreateNew_whenNotExists() {

        UUID userId = UUID.randomUUID();
        UserBillingProfile newProfile = new UserBillingProfile();
        newProfile.setId(UUID.randomUUID());
        newProfile.setUserId(userId);

        when(billingProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(billingProfileRepository.save(any(UserBillingProfile.class))).thenReturn(newProfile);

        BillingProfileResponse result = billingProfileService.getOrCreateBillingProfile(userId);

        assertNotNull(result);
        verify(billingProfileRepository).save(any(UserBillingProfile.class));
    }

    @Test
    void updateBillingProfile_shouldUpdate() {

        UUID userId = UUID.randomUUID();
        UserBillingProfile profile = new UserBillingProfile();
        profile.setId(UUID.randomUUID());
        profile.setUserId(userId);

        when(billingProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(billingProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        billingProfileService.updateBillingProfile(userId, "cus_new", "pm_new");

        assertEquals("cus_new", profile.getCustomerId());
        assertEquals("pm_new", profile.getDefaultPaymentMethodId());
    }
}
