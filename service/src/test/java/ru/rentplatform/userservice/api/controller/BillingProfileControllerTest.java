package ru.rentplatform.userservice.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.rentplatform.userservice.api.dto.response.BillingProfileResponse;
import ru.rentplatform.userservice.core.service.BillingProfileService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class BillingProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BillingProfileService billingProfileService;

    @Test
    void getBillingProfile_shouldReturnProfile() throws Exception {

        BillingProfileResponse profile = BillingProfileResponse.builder()
                .id(UUID.randomUUID()).userId(UUID.randomUUID())
                .customerId("cus_123").defaultPaymentMethodId("pm_456").build();

        when(billingProfileService.getOrCreateBillingProfile(any())).thenReturn(profile);

        mockMvc.perform(get("/api/users/me/billing")
                        .with(jwt().jwt(j -> j.claim("sub", "3227ee7b-775f-4743-8781-5563f352f9a7"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("cus_123"))
                .andExpect(jsonPath("$.defaultPaymentMethodId").value("pm_456"));
    }
}
