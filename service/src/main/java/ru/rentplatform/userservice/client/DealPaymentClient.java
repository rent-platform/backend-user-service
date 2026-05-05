package ru.rentplatform.userservice.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DealPaymentClient {

    private final RestClient dealPaymentServiceRestClient;

    public Double getUserOverallRating(UUID userId) {
        try {
            Map<String, Object> response = dealPaymentServiceRestClient.get()
                    .uri("/api/reviews/users/{userId}/summary", userId)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.get("overallRating") != null) {
                return ((Number) response.get("overallRating")).doubleValue();
            }
        } catch (Exception e) {
            // ignore
        }
        return 0.0;
    }
}
