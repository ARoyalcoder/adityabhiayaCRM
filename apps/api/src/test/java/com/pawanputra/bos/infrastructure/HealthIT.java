package com.pawanputra.bos.infrastructure;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import com.pawanputra.bos.support.IntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

/** Health checks: the public API endpoint and the internal Actuator probes. */
class HealthIT extends IntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void apiHealthReportsDatabaseAndRedisUp() {
        given().accept(ContentType.JSON)
                .when()
                .get("/api/v1/health")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"))
                .body("application", equalTo("Pawan Putra Business OS"))
                .body("version", notNullValue())
                .body("checkedAt", notNullValue())
                .body("components", aMapWithSize(2))
                .body("components.database", equalTo("UP"))
                .body("components.redis", equalTo("UP"));
    }

    @Test
    void apiHealthNeverExposesDetails() {
        given().when()
                .get("/api/v1/health")
                .then()
                .body("$", not(hasKey("details")))
                .body("components", not(hasKey("diskSpace")));
    }

    @Test
    void readinessProbeChecksDatabaseAndRedis() {
        given().when()
                .get("/actuator/health/readiness")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"))
                .body("components.db.status", equalTo("UP"))
                .body("components.redis.status", equalTo("UP"))
                .body("components.db", not(hasKey("details")));
    }

    @Test
    void livenessProbeDoesNotDependOnRedis() {
        given().when()
                .get("/actuator/health/liveness")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"))
                .body("components", not(hasKey("redis")));
    }
}
