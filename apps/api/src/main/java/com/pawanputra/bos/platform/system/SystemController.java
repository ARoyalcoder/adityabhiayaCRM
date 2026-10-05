package com.pawanputra.bos.platform.system;

import com.pawanputra.bos.platform.config.ApplicationProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Clock;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Platform endpoint that lets a client confirm it reached this application.
 * It carries no business data and is not a substitute for the Actuator health
 * endpoints, which stay internal (docs/architecture/12-deployment-architecture.md).
 */
@RestController
@RequestMapping("/api/v1/system")
@Tag(name = "System", description = "Application identity")
class SystemController {

    private final ApplicationProperties properties;
    private final Clock clock;

    SystemController(ApplicationProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    @GetMapping("/info")
    @Operation(summary = "Application name, version and current server time")
    SystemInfoResponse info() {
        return new SystemInfoResponse(properties.name(), properties.version(), clock.instant());
    }
}
