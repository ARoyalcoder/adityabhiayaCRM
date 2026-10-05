package com.pawanputra.bos;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.pawanputra.bos.support.IntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * The application starts against real PostgreSQL and Redis and answers on its
 * platform endpoints with the documented shapes.
 */
class ApplicationStartupIT extends IntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
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
