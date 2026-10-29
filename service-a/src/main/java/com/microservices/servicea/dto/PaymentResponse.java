package com.microservices.servicea.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) returned from Service B (Payment Service).
 */
public class PaymentResponse {

    // Payment Transaction ID
    private Long paymentId;

    // Associated Order ID
    private Long orderId;

    // Payment Reference Number (e.g. PAY-XXXX)
    private String paymentReference;

    // Processed Amount
    private BigDecimal amount;

    // Payment Status: SUCCESS, FAILED, REFUNDED
    private String status;

    // Reason or failure remarks
    private String reason;

    // Distributed Trace ID
    private String traceId;

    public PaymentResponse() {
    }

    public PaymentResponse(Long paymentId, Long orderId, String paymentReference, BigDecimal amount, String status, String reason, String traceId) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.paymentReference = paymentReference;
        this.amount = amount;
        this.status = status;
        this.reason = reason;
        this.traceId = traceId;
    }

    // Getters and Setters
    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
