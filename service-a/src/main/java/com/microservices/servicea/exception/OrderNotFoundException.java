package com.microservices.servicea.exception;

/**
 * Exception thrown when a requested order is not found in the database or cache.
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String message) {
        super(message);
    }
}
