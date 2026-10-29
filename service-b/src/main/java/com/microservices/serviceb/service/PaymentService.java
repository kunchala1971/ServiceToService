package com.microservices.serviceb.service;

import com.microservices.serviceb.dto.PaymentRequest;
import com.microservices.serviceb.dto.PaymentResponse;
import com.microservices.serviceb.entity.PaymentEntity;
import com.microservices.serviceb.entity.PaymentStatus;
import com.microservices.serviceb.repository.PaymentRepository;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Service B (Payment Service) Business Logic.
 * 
 * Features:
 * 1. Payment processing.
 * 2. Compensating Transaction / Refund (Saga Rollback).
 * 3. Caching Techniques (@Cacheable, @CacheEvict).
 * 4. Distributed Tracing (Micrometer Trace ID propagation).
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final Tracer tracer;

    public PaymentService(PaymentRepository paymentRepository, Tracer tracer) {
        this.paymentRepository = paymentRepository;
        this.tracer = tracer;
    }

    private String getTraceId() {
        if (tracer != null && tracer.currentSpan() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return "N/A";
    }

    /**
     * Process payment transaction.
     * 
     * Business rules & failure simulation for Saga compensation testing:
     * 1. If customerId is 'FAIL_CUSTOMER' or amount > 50,000, the payment is rejected.
     *    This simulates a downstream service failure.
     * 2. On success, the record is committed to service_b_db.
     */
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        String traceId = getTraceId();
        log.info("Payment processing started [OrderId: {}, Amount: {}, TraceId: {}]",
                request.getOrderId(), request.getAmount(), traceId);

        // Simulated business validation rule
        if ("FAIL_CUSTOMER".equalsIgnoreCase(request.getCustomerId()) ||
                (request.getAmount() != null && request.getAmount().compareTo(new BigDecimal("50000")) > 0)) {

            log.warn("Payment rejected: Credit limit exceeded or invalid customer [OrderId: {}, TraceId: {}]",
                    request.getOrderId(), traceId);

            PaymentEntity failedPayment = new PaymentEntity(
                    request.getOrderId(),
                    "FAILED-" + UUID.randomUUID().toString().substring(0, 8),
                    request.getCustomerId(),
                    request.getAmount(),
                    PaymentStatus.FAILED,
                    "Insufficient funds or payment limit exceeded."
            );
            paymentRepository.save(failedPayment);

            return new PaymentResponse(
                    failedPayment.getId(),
                    request.getOrderId(),
                    failedPayment.getPaymentReference(),
                    request.getAmount(),
                    "FAILED",
                    failedPayment.getReason(),
                    traceId
            );
        }

        // Happy Path: Payment Authorized and Charged
        String paymentRef = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        PaymentEntity payment = new PaymentEntity(
                request.getOrderId(),
                paymentRef,
                request.getCustomerId(),
                request.getAmount(),
                PaymentStatus.SUCCESS,
                "Payment authorized and charged successfully."
        );

        PaymentEntity saved = paymentRepository.save(payment);
        log.info("Payment saved successfully [PaymentId: {}, Ref: {}, TraceId: {}]",
                saved.getId(), saved.getPaymentReference(), traceId);

        return new PaymentResponse(
                saved.getId(),
                saved.getOrderId(),
                saved.getPaymentReference(),
                saved.getAmount(),
                "SUCCESS",
                saved.getReason(),
                traceId
        );
    }

    /**
     * Compensating Transaction (Reverse/Refund Action in Saga Pattern):
     * 
     * Invoked by the Saga Orchestrator (Service A) when downstream failures require rollback.
     * Reverses the payment and sets status to REFUNDED to maintain Eventual Consistency.
     * 
     * @CacheEvict: Evicts stale payment data from cache.
     */
    @CacheEvict(value = "payments", key = "#request.orderId")
    @Transactional
    public PaymentResponse compensateRefund(PaymentRequest request) {
        String traceId = getTraceId();
        log.warn("[COMPENSATING TRANSACTION] Refund/reversal initiated [OrderId: {}, TraceId: {}]",
                request.getOrderId(), traceId);

        Optional<PaymentEntity> paymentOpt = paymentRepository.findByOrderId(request.getOrderId());

        if (paymentOpt.isPresent()) {
            PaymentEntity payment = paymentOpt.get();
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setReason("Saga compensation: Order cancelled; full amount refunded.");
            PaymentEntity refundedPayment = paymentRepository.save(payment);

            log.info("Payment record updated to REFUNDED [PaymentId: {}]", refundedPayment.getId());

            return new PaymentResponse(
                    refundedPayment.getId(),
                    refundedPayment.getOrderId(),
                    refundedPayment.getPaymentReference(),
                    refundedPayment.getAmount(),
                    "REFUNDED",
                    refundedPayment.getReason(),
                    traceId
            );
        } else {
            log.info("No prior payment found to refund [OrderId: {}]", request.getOrderId());
            return new PaymentResponse(
                    null,
                    request.getOrderId(),
                    "NO-PAYMENT",
                    request.getAmount(),
                    "REFUNDED",
                    "No prior payment record existed; compensation complete.",
                    traceId
            );
        }
    }

    /**
     * Caching Technique: @Cacheable
     * 
     * Queries payment details by Order ID.
     * The first call fetches from MySQL (service_b_db) and caches the result.
     * Subsequent queries return instantly from memory cache.
     */
    @Cacheable(value = "payments", key = "#orderId")
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        String traceId = getTraceId();
        log.info("[CACHE MISS] Querying payment details from service_b_db [OrderId: {}]", orderId);

        PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment record not found for Order ID " + orderId));

        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getPaymentReference(),
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getReason(),
                traceId
        );
    }
}
