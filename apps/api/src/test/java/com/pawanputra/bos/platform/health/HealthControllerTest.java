package com.pawanputra.bos.platform.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pawanputra.bos.platform.config.ApplicationProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class HealthControllerTest {

    private static final Instant NOW = Instant.parse("2026-10-05T07:15:00Z");

    private final HealthEndpoint healthEndpoint = mock(HealthEndpoint.class);
    private final HealthController controller = new HealthController(
            healthEndpoint,
            new ApplicationProperties("Pawan Putra Business OS", "0.1.0"),
            Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void returns200AndPublishesOnlyDatabaseAndRedisWhenEverythingIsUp() {
        HealthComponent health = composite(Status.UP, Map.of(
                "db", Health.up().withDetail("database", "PostgreSQL").build(),
                "redis", Health.up().build(),
                "diskSpace", Health.up().build()));
        when(healthEndpoint.health()).thenReturn(health);

        ResponseEntity<HealthResponse> response = controller.health();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("UP");
        assertThat(response.getBody().components())
                .containsExactly(Map.entry("database", "UP"), Map.entry("redis", "UP"));
        assertThat(response.getBody().checkedAt()).isEqualTo(NOW);
    }

    @Test
    void returns503WhenRedisIsDown() {
        HealthComponent health = composite(Status.DOWN, Map.of(
                "db", Health.up().build(),
                "redis", Health.down().withDetail("error", "Connection refused").build()));
        when(healthEndpoint.health()).thenReturn(health);

        ResponseEntity<HealthResponse> response = controller.health();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("DOWN");
        assertThat(response.getBody().components()).containsEntry("redis", "DOWN");
    }

    @Test
    void returns503WhenOutOfService() {
        when(healthEndpoint.health()).thenReturn(Health.status(Status.OUT_OF_SERVICE).build());

        ResponseEntity<HealthResponse> response = controller.health();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().components()).isEmpty();
    }

    private static HealthComponent composite(Status status, Map<String, HealthComponent> components) {
        CompositeHealth composite = mock(CompositeHealth.class);
        when(composite.getStatus()).thenReturn(status);
        when(composite.getComponents()).thenReturn(components);
        return composite;
    }
}
