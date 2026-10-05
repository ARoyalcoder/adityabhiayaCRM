package com.pawanputra.bos.platform.health;

import com.pawanputra.bos.platform.config.ApplicationProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public application health under the versioned API, reachable through Nginx.
 * It reuses the Actuator health checks, so the answer here and on the internal
 * {@code /actuator/health} can never disagree, and it exposes only statuses:
 * no host names, versions or error messages.
 */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "System", description = "Application identity and health")
class HealthController {

    /** Actuator contributor name → name shown to API clients. */
    private static final Map<String, String> PUBLISHED_COMPONENTS = Map.of(
            "db", "database",
            "redis", "redis");

    private final HealthEndpoint healthEndpoint;
    private final ApplicationProperties properties;
    private final Clock clock;

    HealthController(HealthEndpoint healthEndpoint, ApplicationProperties properties, Clock clock) {
        this.healthEndpoint = healthEndpoint;
        this.properties = properties;
        this.clock = clock;
    }

    @GetMapping
    @Operation(summary = "Overall health and the status of the database and Redis")
    @ApiResponse(responseCode = "200", description = "Application and all dependencies are up")
    @ApiResponse(responseCode = "503", description = "At least one dependency is not up")
    ResponseEntity<HealthResponse> health() {
        HealthComponent health = healthEndpoint.health();

        Map<String, String> components = new LinkedHashMap<>();
        if (health instanceof CompositeHealth composite) {
            composite.getComponents().entrySet().stream()
                    .filter(entry -> PUBLISHED_COMPONENTS.containsKey(entry.getKey()))
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> components.put(
                            PUBLISHED_COMPONENTS.get(entry.getKey()), entry.getValue().getStatus().getCode()));
        }

        HealthResponse body = new HealthResponse(
                health.getStatus().getCode(),
                properties.name(),
                properties.version(),
                clock.instant(),
                components);
        HttpStatus httpStatus = Status.UP.equals(health.getStatus()) ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(httpStatus).body(body);
    }
}
