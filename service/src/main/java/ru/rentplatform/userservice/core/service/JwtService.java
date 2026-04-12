package ru.rentplatform.userservice.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import ru.rentplatform.userservice.config.JwtProperties;
import ru.rentplatform.userservice.core.dao.entity.User;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(jwtProperties.getAccessTokenExpirationSeconds());

        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("rent-platform-user-service")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getId().toString())
                .claim("nickname", user.getNickname())
                .claim("role", user.getRole())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateRefreshToken() {
        return UUID.randomUUID() + "." + UUID.randomUUID();
    }

    public long getAccessTokenExpirationSeconds() {
        return jwtProperties.getAccessTokenExpirationSeconds();
    }
}
