package com.microservices.serviceb.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment JPA Entity representing the 'payments' table in service_b_db.
 * Implements Serializable for in-memory caching.
 */
@Entity
@Table(name = "payments")
public class PaymentEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    // Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Associated Order ID from Service A
    @Column(nullable = false)
    private Long orderId;

    // Unique payment reference code (e.g. PAY-XXXX)
    @Column(nullable = false, unique = true, length = 64)
    private String paymentReference;

    // Customer Identifier
    @Column(nullable = false)
    private String customerId;

    // Payment Amount
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // Payment Status: SUCCESS, FAILED, REFUNDED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status;

    // Details or failure reason
    @Column(length = 255)
    private String reason;

    // Creation timestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Last updated timestamp
    private LocalDateTime updatedAt;

    public PaymentEntity() {
    }

    public PaymentEntity(Long orderId, String paymentReference, String customerId, BigDecimal amount, PaymentStatus status, String reason) {
        this.orderId = orderId;
        this.paymentReference = paymentReference;
        this.customerId = customerId;
        this.amount = amount;
        this.status = status;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
