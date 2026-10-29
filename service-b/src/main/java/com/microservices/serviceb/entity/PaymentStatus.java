package com.microservices.serviceb.entity;

/**
 * Payment Status enumeration for distributed transaction lifecycle management.
 */
public enum PaymentStatus {
    // 1. SUCCESS: Payment successfully authorized and charged.
    SUCCESS,

    // 2. FAILED: Insufficient funds or business rule violation.
    FAILED,

    // 3. REFUNDED: Reversed/refunded via Saga Compensating Transaction.
    REFUNDED
}
