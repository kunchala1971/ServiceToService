package com.microservices.servicea.exception;

import com.microservices.servicea.dto.ApiResponse;
import feign.FeignException;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global Exception Handler / Error Catching Techniques.
 * 
 * Catches all internal and downstream microservice exceptions,
 * and formats them into a standardized API response with appropriate HTTP status codes
 * and the active distributed Trace ID.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Tracer tracer;

    public GlobalExceptionHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    private String getTraceId() {
        if (tracer != null && tracer.currentSpan() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return "NO-TRACE-ID";
    }

    /**
     * Catching OrderNotFoundException (HTTP 404 Not Found).
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleOrderNotFound(OrderNotFoundException ex) {
        String traceId = getTraceId();
        log.warn("Order not found [Trace ID: {}]: {}", traceId, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), traceId));
    }

    /**
     * Catching PaymentFailedException (HTTP 422 Unprocessable Entity).
     */
    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<ApiResponse<Void>> handlePaymentFailed(PaymentFailedException ex) {
        String traceId = getTraceId();
        log.error("Payment processing failed [Trace ID: {}]: {}", traceId, ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error("Payment failure: " + ex.getMessage() + " (Reason: " + ex.getReason() + ")", traceId));
    }

    /**
     * Catching OpenFeign Exceptions (HTTP 502 Bad Gateway)
     * Triggered when downstream Service B is unreachable or returns a server error.
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiResponse<Void>> handleFeignException(FeignException ex) {
        String traceId = getTraceId();
        log.error("Downstream service communication failure [Trace ID: {}]: HTTP Status={}, Error={}",
                traceId, ex.status(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.error("Downstream Payment Service unavailable or failed: " + ex.getMessage(), traceId));
    }

    /**
     * Catching all generic unhandled exceptions (HTTP 500 Internal Server Error).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        String traceId = getTraceId();
        log.error("Unhandled internal server error occurred [Trace ID: {}]: ", traceId, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error: " + ex.getMessage(), traceId));
    }
}
