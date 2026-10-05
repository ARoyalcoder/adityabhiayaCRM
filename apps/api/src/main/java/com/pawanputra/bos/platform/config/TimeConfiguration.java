package com.pawanputra.bos.platform.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * A single injectable clock, so time-dependent rules are testable and all
 * stored moments are UTC (docs/architecture/04-backend-architecture.md).
 */
@Configuration
class TimeConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
