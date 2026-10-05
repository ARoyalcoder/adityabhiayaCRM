package com.pawanputra.bos.platform.system;

import static org.assertj.core.api.Assertions.assertThat;

import com.pawanputra.bos.platform.config.ApplicationProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class SystemControllerTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-10-05T07:15:00Z");

    private final SystemController controller = new SystemController(
            new ApplicationProperties("Pawan Putra Business OS", "0.1.0-SNAPSHOT"),
            Clock.fixed(FIXED_NOW, ZoneOffset.UTC));

    @Test
    void reportsConfiguredIdentityAndInjectedTime() {
        SystemInfoResponse response = controller.info();

        assertThat(response.application()).isEqualTo("Pawan Putra Business OS");
        assertThat(response.version()).isEqualTo("0.1.0-SNAPSHOT");
        assertThat(response.serverTime()).isEqualTo(FIXED_NOW);
    }
}
