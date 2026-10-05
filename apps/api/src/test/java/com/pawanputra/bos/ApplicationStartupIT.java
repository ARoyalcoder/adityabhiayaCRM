package com.pawanputra.bos;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.pawanputra.bos.support.PostgresIntegrationTest;
import com.pawanputra.bos.support.TestcontainersConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Proves the scaffold works end to end on the server side: the context starts,
 * Flyway migrates a real PostgreSQL database, the health probes answer and the
 * system endpoint returns its documented shape with a request id.
 */
@Import(TestcontainersConfiguration.class)
class ApplicationStartupIT extends PostgresIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void flywayCreatesThePlatformSchema() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        Integer applied = jdbc.queryForObject(
                "SELECT count(*) FROM platform.flyway_schema_history WHERE success", Integer.class);
        String schema = jdbc.queryForObject(
                "SELECT schema_name FROM information_schema.schemata WHERE schema_name = 'platform'", String.class);

        assertThat(applied).isPositive();
        assertThat(schema).isEqualTo("platform");
    }

    @Test
    void readinessProbeReportsUp() {
        given().when()
                .get("/actuator/health/readiness")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    @Test
    void systemInfoReturnsIdentityAndRequestId() {
        given().accept(ContentType.JSON)
                .when()
                .get("/api/v1/system/info")
                .then()
                .statusCode(200)
                .header("X-Request-Id", notNullValue())
                .body("application", equalTo("Pawan Putra Business OS"))
                .body("version", notNullValue())
                .body("serverTime", notNullValue());
    }

    @Test
    void unknownApiPathReturnsProblemDetail() {
        given().accept(ContentType.JSON)
                .when()
                .get("/api/v1/system/does-not-exist")
                .then()
                .statusCode(404)
                .contentType("application/problem+json")
                .body("code", equalTo("UNKNOWN_ENDPOINT"))
                .body("requestId", notNullValue())
                // The internal resolution detail must not reach the client.
                .body("detail", equalTo("This endpoint does not exist."));
    }
}
