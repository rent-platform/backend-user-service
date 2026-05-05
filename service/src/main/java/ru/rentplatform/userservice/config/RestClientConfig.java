package ru.rentplatform.userservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient dealPaymentServiceRestClient(
            @Value("${app.deal-payment-service-url:http://localhost:8083}") String url) {
        return RestClient.create(url);
    }
}
