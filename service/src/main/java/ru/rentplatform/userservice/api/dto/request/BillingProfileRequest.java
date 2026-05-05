package ru.rentplatform.userservice.api.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingProfileRequest {

    private String customerId;

    private String paymentMethodId;
}
