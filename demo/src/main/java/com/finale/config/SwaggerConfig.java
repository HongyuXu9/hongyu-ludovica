package com.finale.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API Presenze",
                version = "1.0",
                description = "Gestione iscrizioni presenze"
        )
)
public class SwaggerConfig {
}