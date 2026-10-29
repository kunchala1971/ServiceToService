package com.microservices.servicea.entity;

/**
 * Order Status enumeration for the Saga state machine.
 */
public enum OrderStatus {
    // 1. PENDING: Order request initiated, awaiting downstream payment processing.
    PENDING,

    // 2. CONFIRMED: Downstream payment processed successfully; order confirmed.
    CONFIRMED,

    // 3. CANCELLED_PAYMENT_FAILED: Downstream payment failed; local order compensated and cancelled.
    CANCELLED_PAYMENT_FAILED,

    // 4. COMPENSATED: Distributed rollback completed across all services; eventual consistency achieved.
    COMPENSATED
}
