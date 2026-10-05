package com.pawanputra.bos.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.pawanputra.bos.support.IntegrationTest;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/** Redis: the connection works and values round-trip with an expiry. */
class RedisIT extends IntegrationTest {

    @Autowired
    private StringRedisTemplate redis;

    @Autowired
    private RedisConnectionFactory connectionFactory;

    @Test
    void answersPing() {
        try (var connection = connectionFactory.getConnection()) {
            assertThat(connection.ping()).isEqualTo("PONG");
        }
    }

    @Test
    void storesAndReadsAValueWithExpiry() {
        String key = "test:" + UUID.randomUUID();

        redis.opsForValue().set(key, "value", Duration.ofSeconds(30));

        assertThat(redis.opsForValue().get(key)).isEqualTo("value");
        assertThat(redis.getExpire(key)).isBetween(1L, 30L);
        assertThat(redis.delete(key)).isTrue();
    }
}
