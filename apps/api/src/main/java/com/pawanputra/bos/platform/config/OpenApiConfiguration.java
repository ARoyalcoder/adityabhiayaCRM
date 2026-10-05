package com.pawanputra.bos.platform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The OpenAPI document is the contract the frontend's typed API functions and
 * Zod schemas follow (docs/architecture/03-frontend-architecture.md, D-03.3).
 * It is served only outside production, configured in application.yml.
 */
@Configuration
class OpenApiConfiguration {

    @Bean
    OpenAPI openApi(ApplicationProperties properties) {
        return new OpenAPI().info(new Info()
                .title(properties.name() + " API")
                .version(properties.version())
                .description("REST API of Pawan Putra Business OS. All endpoints live under /api/v1."));
    }
}
