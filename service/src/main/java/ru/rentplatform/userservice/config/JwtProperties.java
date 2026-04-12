package ru.rentplatform.userservice.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Getter
@Setter
@AllArgsConstructor
public class JwtProperties {

    private final long accessTokenExpirationSeconds;

    private final long refreshTokenExpirationSeconds;

    private final RSAPrivateKey privateKey;

    private final RSAPublicKey publicKey;
}
