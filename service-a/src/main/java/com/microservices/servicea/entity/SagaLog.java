package com.microservices.servicea.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Saga Transaction Audit Log Entity.
 * Maps to the 'saga_logs' table in service_a_db to record every distributed step,
 * status transition, error details, and associated Trace ID for auditability.
 */
@Entity
@Table(name = "saga_logs")
public class SagaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Associated Order ID
    @Column(nullable = false)
    private Long orderId;

    // Distributed Trace ID for end-to-end correlation
    @Column(length = 64)
    private String traceId;

    // Saga step identifier (e.g. ORDER_CREATED, PAYMENT_CONFIRMED, ORDER_COMPENSATED_LOCAL)
    @Column(nullable = false, length = 64)
    private String step;

    // Step execution status (SUCCESS or FAILED)
    @Column(nullable = false, length = 32)
    private String status;

    // Descriptive message or failure cause
    @Column(length = 500)
    private String message;

    // Timestamp of step execution
    @Column(nullable = false)
    private LocalDateTime timestamp;

    public SagaLog() {
    }

    public SagaLog(Long orderId, String traceId, String step, String status, String message) {
        this.orderId = orderId;
        this.traceId = traceId;
        this.step = step;
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
