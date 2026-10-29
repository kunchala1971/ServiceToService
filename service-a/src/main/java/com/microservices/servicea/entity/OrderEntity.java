package com.microservices.servicea.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order JPA Entity representing the 'orders' table in service_a_db.
 * Implements Serializable for seamless in-memory caching.
 */
@Entity
@Table(name = "orders")
public class OrderEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    // Primary Key: Auto-incremented ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique Order Tracking Number (e.g. ORD-XXXX)
    @Column(nullable = false, unique = true, length = 64)
    private String orderNumber;

    // Customer Identifier
    @Column(nullable = false)
    private String customerId;

    // Product Identifier
    @Column(nullable = false)
    private String productId;

    // Total Order Amount
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // Order status: PENDING, CONFIRMED, CANCELLED_PAYMENT_FAILED, etc.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status;

    // Record creation timestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Record last-update timestamp
    private LocalDateTime updatedAt;

    // Default Constructor required by JPA
    public OrderEntity() {
    }

    public OrderEntity(String orderNumber, String customerId, String productId, BigDecimal amount, OrderStatus status) {
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.productId = productId;
        this.amount = amount;
        this.status = status;
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

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
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

    @Override
    public String toString() {
        return "OrderEntity{" +
                "id=" + id +
                ", orderNumber='" + orderNumber + '\'' +
                ", customerId='" + customerId + '\'' +
                ", productId='" + productId + '\'' +
                ", amount=" + amount +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
