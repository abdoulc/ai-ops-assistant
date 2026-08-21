package org.abdel.aiops.api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
    private static final Logger log =
            LoggerFactory.getLogger(CorrelationIdFilter.class);
    static final String MDC_KEY = "correlationId";
    static final String HEADER_NAME = "X-Correlation-ID";
    private static final Pattern VALID_CORRELATION_ID =
            Pattern.compile("^[A-Za-z0-9._-]{1,100}$");
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String incomingCorrelationId =
                request.getHeader(HEADER_NAME);
        String correlationId =
                isValidCorrelationId(incomingCorrelationId)
                        ? incomingCorrelationId
                        : UUID.randomUUID().toString();
        long startNanos = System.nanoTime();
        MDC.put(MDC_KEY, correlationId);
        try{
            response.setHeader(
                    HEADER_NAME,
                    correlationId
            );

            filterChain.doFilter(request, response);
        }finally {
            long durationMs =
                    (System.nanoTime() - startNanos) / 1_000_000;

            log.info(
                    "HTTP request completed method={} path={} status={} durationMs={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs
            );
            MDC.remove(MDC_KEY);
        }

    }

    private boolean isValidCorrelationId(String correlationId) {
        return correlationId != null
                && VALID_CORRELATION_ID.matcher(correlationId).matches();
    }
}
