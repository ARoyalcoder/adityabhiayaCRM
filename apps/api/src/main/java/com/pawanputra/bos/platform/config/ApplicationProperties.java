package com.pawanputra.bos.platform.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Technical application settings. Configurable business rules belong in the
 * database, not here (project rule 5).
 *
 * @param name    human-readable application name, shown by the system endpoint
 * @param version build version, shown by the system endpoint and in support requests
 */
@Validated
@ConfigurationProperties(prefix = "app")
public record ApplicationProperties(@NotBlank String name, @NotBlank String version) {
}
