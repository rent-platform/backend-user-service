package ru.rentplatform.userservice.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    @Bean
    public OpenAPI userServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Service API")
                        .description("API для управления пользователями Rent Platform")
                        .version("1.0.0"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Gateway"),
                        new Server().url("http://localhost:8081").description("User Service (Direct)")
                ));
    }
}
