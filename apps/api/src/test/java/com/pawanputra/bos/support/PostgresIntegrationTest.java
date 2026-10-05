package com.pawanputra.bos.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base for tests that need the application running against a real PostgreSQL
 * container, which is how migrations and PostgreSQL-specific features are
 * verified (docs/architecture/11-testing-architecture.md, D-11.1).
 */
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class PostgresIntegrationTest {
}
