package com.microservices.servicea.exception;

/**
 * Custom Exception thrown when downstream payment processing fails.
 * Caught by the Saga Orchestrator to trigger compensating rollback actions.
 */
public class PaymentFailedException extends RuntimeException {

    private final String reason;

    public PaymentFailedException(String message, String reason) {
        super(message);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
