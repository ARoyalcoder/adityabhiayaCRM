package com.pawanputra.bos.platform.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Gives every request an id, taken from Nginx's {@code X-Request-Id} when
 * present. The id goes into the logging context, the response header and error
 * bodies, so a user-reported problem can be found in the logs
 * (docs/architecture/04-backend-architecture.md).
 */
@Component
@Order(RequestIdFilter.ORDER)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final int ORDER = Integer.MIN_VALUE + 10;
    public static final String HEADER = "X-Request-Id";
    public static final String MDC_KEY = "requestId";

    private static final int MAX_LENGTH = 64;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestId = sanitize(request.getHeader(HEADER));
        request.setAttribute(MDC_KEY, requestId);
        response.setHeader(HEADER, requestId);
        MDC.put(MDC_KEY, requestId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    /** Never trusts an inbound header blindly: it ends up in logs and responses. */
    private static String sanitize(String incoming) {
        if (!StringUtils.hasText(incoming)) {
            return UUID.randomUUID().toString();
        }
        String trimmed = incoming.trim();
        if (trimmed.length() > MAX_LENGTH || !trimmed.matches("[A-Za-z0-9._:-]+")) {
            return UUID.randomUUID().toString();
        }
        return trimmed;
    }
}
