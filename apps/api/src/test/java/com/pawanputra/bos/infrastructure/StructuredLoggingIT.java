package com.pawanputra.bos.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pawanputra.bos.platform.web.RequestIdFilter;
import com.pawanputra.bos.support.IntegrationTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

/** Logs are one JSON object per line and carry the request id. */
@ExtendWith(OutputCaptureExtension.class)
class StructuredLoggingIT extends IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(StructuredLoggingIT.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    void writesEcsJsonWithServiceNameAndRequestId(CapturedOutput output) throws Exception {
        MDC.put(RequestIdFilter.MDC_KEY, "log-check-1");
        try {
            log.info("structured logging check");
        } finally {
            MDC.remove(RequestIdFilter.MDC_KEY);
        }

        JsonNode line = findLine(output, "structured logging check");
        assertThat(line.at("/log/level").asText()).isEqualTo("INFO");
        assertThat(line.at("/service/name").asText()).isEqualTo("business-os-api");
        assertThat(line.at("/service/environment").asText()).isEqualTo("test");
        assertThat(line.path("requestId").asText()).isEqualTo("log-check-1");
        assertThat(line.has("@timestamp")).isTrue();
    }

    private static JsonNode findLine(CapturedOutput output, String message) throws Exception {
        for (JsonNode node : jsonLines(output)) {
            if (message.equals(node.path("message").asText())) {
                return node;
            }
        }
        throw new AssertionError("No JSON log line with message: " + message + "\n" + output.getOut());
    }

    private static List<JsonNode> jsonLines(CapturedOutput output) throws Exception {
        List<String> raw = Arrays.stream(output.getOut().split("\\R"))
                .filter(line -> line.startsWith("{"))
                .toList();
        List<JsonNode> nodes = new ArrayList<>();
        for (String line : raw) {
            nodes.add(JSON.readTree(line));
        }
        return nodes;
    }
}
