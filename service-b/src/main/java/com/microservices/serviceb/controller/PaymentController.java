package com.microservices.serviceb.controller;

import com.microservices.serviceb.dto.PaymentRequest;
import com.microservices.serviceb.dto.PaymentResponse;
import com.microservices.serviceb.service.PaymentService;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Service B (Payment Service) REST Controller.
 * Handles incoming synchronous requests from Service A's OpenFeign client.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;
    private final Tracer tracer;

    public PaymentController(PaymentService paymentService, Tracer tracer) {
        this.paymentService = paymentService;
        this.tracer = tracer;
    }

    private String getTraceId() {
        if (tracer != null && tracer.currentSpan() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return "N/A";
    }

    /**
     * Endpoint to process payments.
     */
    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        String traceId = getTraceId();
        log.info("REST Call: Payment processing request received [OrderId: {}, TraceId: {}]",
                request.getOrderId(), traceId);

        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint for Compensating Transactions (Reverse / Refund Action).
     */
    @PostMapping("/compensate-refund")
    public ResponseEntity<PaymentResponse> compensateRefund(@RequestBody PaymentRequest request) {
        String traceId = getTraceId();
        log.warn("REST Call: Compensating refund request received [OrderId: {}, TraceId: {}]",
                request.getOrderId(), traceId);

        PaymentResponse response = paymentService.compensateRefund(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cached endpoint to retrieve payment by Order ID.
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable("orderId") Long orderId) {
        String traceId = getTraceId();
        log.info("REST Call: Querying payment details [OrderId: {}, TraceId: {}]", orderId, traceId);

        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(response);
    }
}
