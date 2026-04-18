package ru.rentplatform.userservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.session.cleanup")
public class SessionCleanupProperties {

    private long delayMs;
    private int revokedRetentionDays;
}