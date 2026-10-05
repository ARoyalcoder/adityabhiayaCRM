package com.pawanputra.bos.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.pawanputra.bos.support.IntegrationTest;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/** PostgreSQL: the connection works and Flyway has migrated the schema. */
class DatabaseIT extends IntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void connectsToPostgresql() {
        String version = new JdbcTemplate(dataSource).queryForObject("SHOW server_version", String.class);

        assertThat(version).startsWith("16.");
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
}
