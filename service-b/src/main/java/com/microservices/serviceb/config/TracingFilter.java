package com.microservices.serviceb.config;

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
 * Service B Distributed Tracing & Correlation ID Servlet Filter.
 * 
 * Extracts the incoming Correlation ID / Trace ID passed by Service A via OpenFeign headers,
 * binds it to the current MDC context and logs, and echoes it in HTTP response headers.
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

        // Retrieve Correlation ID from upstream request headers
        String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Retrieve current active Trace ID
        String traceId = (tracer != null && tracer.currentSpan() != null)
                ? tracer.currentSpan().context().traceId()
                : correlationId;

        MDC.put("correlationId", correlationId);
        MDC.put("traceId", traceId);

        httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);
        httpResponse.setHeader(TRACE_ID_HEADER, traceId);

        log.info("[Service B] Request received [TraceId: {}, URI: {}]", traceId, httpRequest.getRequestURI());

        try {
            chain.doFilter(request, response);
        } finally {
            log.info("[Service B] Request completed [TraceId: {}, Status: {}]", traceId, httpResponse.getStatus());
            MDC.remove("correlationId");
            MDC.remove("traceId");
        }
    }
}
