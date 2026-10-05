package com.pawanputra.bos.platform.health;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;

/**
 * Response of {@code GET /api/v1/health}.
 *
 * @param status      overall status: {@code UP}, {@code DOWN}, {@code OUT_OF_SERVICE} or {@code UNKNOWN}
 * @param application human-readable application name
 * @param version     build version
 * @param checkedAt   when the checks ran, ISO-8601 UTC
 * @param components  status of each dependency, by name ({@code database}, {@code redis})
 */
@Schema(description = "Health of the application and the services it depends on")
public record HealthResponse(
        String status,
        String application,
        String version,
        Instant checkedAt,
        Map<String, String> components) {
}
