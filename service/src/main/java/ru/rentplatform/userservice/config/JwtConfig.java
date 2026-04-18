package ru.rentplatform.userservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtConfig {

    @Bean
    public JwtProperties jwtProperties(

            @Value("${security.jwt.access-token-expiration-seconds:900}")
            long accessTokenExpirationSeconds,

            @Value("${security.jwt.refresh-token-short-expiration-seconds:86400}")
            long refreshTokenShortExpirationSeconds,

            @Value("${security.jwt.refresh-token-remember-me-expiration-seconds:2592000}")
            long refreshTokenRememberMeExpirationSeconds,

            RSAPrivateKey privateKey,
            RSAPublicKey publicKey
    ) {
        return new JwtProperties(

                accessTokenExpirationSeconds,
                refreshTokenShortExpirationSeconds,
                refreshTokenRememberMeExpirationSeconds,
                privateKey,
                publicKey
        );
    }
}