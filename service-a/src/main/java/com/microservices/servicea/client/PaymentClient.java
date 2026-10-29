package com.microservices.servicea.client;

import com.microservices.servicea.dto.PaymentRequest;
import com.microservices.servicea.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Spring Cloud OpenFeign Declarative HTTP Client.
 * Enables synchronous inter-service communication from Service A to Service B (Payment Service).
 * 
 * 1. Replaces manual RestTemplate / WebClient boilerplate with a clean interface.
 * 2. Feign-Micrometer automatically propagates Trace ID and Span ID HTTP headers.
 */
@FeignClient(name = "payment-service", url = "${service-b.url:http://localhost:8082}")
public interface PaymentClient {

    /**
     * Synchronously process payment for an order in Service B.
     */
    @PostMapping("/api/payments/process")
    PaymentResponse processPayment(@RequestBody PaymentRequest paymentRequest);

    /**
     * Compensating Transaction (Reverse Action / Refund):
     * Invoked when downstream errors occur to reverse previously committed payments
     * and maintain eventual consistency.
     */
    @PostMapping("/api/payments/compensate-refund")
    PaymentResponse compensateRefund(@RequestBody PaymentRequest paymentRequest);

    /**
     * Retrieve payment details by Order ID.
     */
    @GetMapping("/api/payments/order/{orderId}")
    PaymentResponse getPaymentByOrderId(@PathVariable("orderId") Long orderId);
}
