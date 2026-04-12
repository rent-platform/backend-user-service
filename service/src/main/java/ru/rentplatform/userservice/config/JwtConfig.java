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

            @Value("${security.jwt.access-token-expiration-seconds}")
            long accessTokenExpirationSeconds,

            @Value("${security.jwt.refresh-token-expiration-seconds}")
            long refreshTokenExpirationSeconds,

            RSAPrivateKey privateKey,

            RSAPublicKey publicKey
    ) {
        return new JwtProperties(

                accessTokenExpirationSeconds,

                refreshTokenExpirationSeconds,

                privateKey,

                publicKey
        );
    }
}