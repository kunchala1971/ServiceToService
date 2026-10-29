package com.microservices.servicea.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) for creating a new order.
 */
public class CreateOrderRequest {

    // Customer Identifier
    private String customerId;

    // Product Identifier
    private String productId;

    // Order Amount
    private BigDecimal amount;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(String customerId, String productId, BigDecimal amount) {
        this.customerId = customerId;
        this.productId = productId;
        this.amount = amount;
    }

    // Getters and Setters
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
}
