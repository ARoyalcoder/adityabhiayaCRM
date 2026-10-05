package com.pawanputra.bos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point of the modular monolith. Business modules are added as sibling
 * packages of {@code platform} (docs/architecture/04-backend-architecture.md);
 * none exists yet.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class BusinessOsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusinessOsApplication.class, args);
    }
}
