package com.pawanputra.bos.platform.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @Test
    void keepsTheRequestIdSuppliedByTheProxy() throws Exception {
        MockHttpServletResponse response = filter("req-123_ab:cd");

        assertThat(response.getHeader(RequestIdFilter.HEADER)).isEqualTo("req-123_ab:cd");
    }

    @Test
    void generatesAnIdWhenTheHeaderIsMissing() throws Exception {
        MockHttpServletResponse response = filter(null);

        assertThat(UUID.fromString(response.getHeader(RequestIdFilter.HEADER))).isNotNull();
    }

    @Test
    void replacesAnIdThatCouldPoisonLogsOrResponses() throws Exception {
        MockHttpServletResponse response = filter("bad id\nwith newline");

        assertThat(UUID.fromString(response.getHeader(RequestIdFilter.HEADER))).isNotNull();
    }

    @Test
    void replacesAnOverlongId() throws Exception {
        MockHttpServletResponse response = filter("x".repeat(65));

        assertThat(UUID.fromString(response.getHeader(RequestIdFilter.HEADER))).isNotNull();
    }

    private MockHttpServletResponse filter(String incomingHeader) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/system/info");
        if (incomingHeader != null) {
            request.addHeader(RequestIdFilter.HEADER, incomingHeader);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
