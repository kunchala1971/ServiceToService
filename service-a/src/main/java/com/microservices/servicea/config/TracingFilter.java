package com.microservices.servicea.config;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Distributed Tracing & Correlation ID Servlet Filter.
 * 
 * 1. Attaches a unique Correlation ID / Trace ID to every incoming HTTP request.
 * 2. Uses Micrometer Tracing to retrieve or generate trace context.
 * 3. Injects X-Correlation-Id and X-Trace-Id into HTTP response headers.
 * 4. Populates logging MDC so logs across all microservices can be correlated effortlessly.
 */
@Component
@Order(1)
public class TracingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(TracingFilter.class);
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    private final Tracer tracer;

    public TracingFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Retrieve existing Correlation ID from HTTP headers or generate a new UUID
        String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Retrieve current Trace ID from Micrometer Tracer
        String traceId = (tracer != null && tracer.currentSpan() != null)
                ? tracer.currentSpan().context().traceId()
                : correlationId;

        // Populate MDC for consistent log output
        MDC.put("correlationId", correlationId);
        MDC.put("traceId", traceId);

        // Echo tracking headers back in the HTTP response
        httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);
        httpResponse.setHeader(TRACE_ID_HEADER, traceId);

        log.info("Request started [TraceId: {}, Method: {}, URI: {}]",
                traceId, httpRequest.getMethod(), httpRequest.getRequestURI());

        try {
            chain.doFilter(request, response);
        } finally {
            log.info("Request completed [TraceId: {}, Status: {}]", traceId, httpResponse.getStatus());
            MDC.remove("correlationId");
            MDC.remove("traceId");
        }
    }
}
