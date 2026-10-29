package com.microservices.servicea.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) sent from Service A to Service B for payment processing.
 */
public class PaymentRequest {

    // Associated Order ID
    private Long orderId;

    // Order Tracking Number
    private String orderNumber;

    // Customer Identifier
    private String customerId;

    // Amount to be processed
    private BigDecimal amount;

    public PaymentRequest() {
    }

    public PaymentRequest(Long orderId, String orderNumber, String customerId, BigDecimal amount) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.amount = amount;
    }

    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
