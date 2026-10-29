package com.microservices.servicea.saga;

import com.microservices.servicea.client.PaymentClient;
import com.microservices.servicea.dto.PaymentRequest;
import com.microservices.servicea.dto.PaymentResponse;
import com.microservices.servicea.entity.OrderEntity;
import com.microservices.servicea.entity.OrderStatus;
import com.microservices.servicea.entity.SagaLog;
import com.microservices.servicea.exception.PaymentFailedException;
import com.microservices.servicea.repository.OrderRepository;
import com.microservices.servicea.repository.SagaLogRepository;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Saga Pattern Orchestrator.
 * 
 * In distributed microservices, standard @Transactional cannot span across independent
 * databases (service_a_db and service_b_db).
 * 
 * This Orchestration Saga coordinates the distributed workflow:
 * 1. Step 1: Save Order in local DB (service_a_db) with status PENDING.
 * 2. Step 2: Invoke downstream Payment Service (Service B) via Spring Cloud OpenFeign.
 * 3. Step 3 (Success): Update order status to CONFIRMED.
 * 4. Step 4 (Downstream Failure / Compensation): Catch exception, execute compensating transactions:
 *    - Update local order status to CANCELLED_PAYMENT_FAILED.
 *    - Invoke Service B's compensating refund endpoint (/api/payments/compensate-refund).
 *    - Achieve Eventual Consistency without distributed two-phase locking.
 */
@Component
public class OrderSagaOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(OrderSagaOrchestrator.class);

    private final OrderRepository orderRepository;
    private final SagaLogRepository sagaLogRepository;
    private final PaymentClient paymentClient;
    private final Tracer tracer;

    public OrderSagaOrchestrator(OrderRepository orderRepository,
                                  SagaLogRepository sagaLogRepository,
                                  PaymentClient paymentClient,
                                  Tracer tracer) {
        this.orderRepository = orderRepository;
        this.sagaLogRepository = sagaLogRepository;
        this.paymentClient = paymentClient;
        this.tracer = tracer;
    }

    private String currentTraceId() {
        if (tracer != null && tracer.currentSpan() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return "N/A";
    }

    /**
     * Executes the Order Saga orchestration flow.
     */
    public OrderEntity executeOrderSaga(OrderEntity order) {
        String traceId = currentTraceId();
        log.info("[SAGA START] Order Saga initiated [OrderId: {}, TraceId: {}]", order.getId(), traceId);

        // Step 1: Record initial order creation in saga log
        recordSagaStep(order.getId(), traceId, "ORDER_CREATED", "SUCCESS",
                "Order created in PENDING status within service_a_db.");

        PaymentResponse paymentResponse = null;

        try {
            // Step 2: Invoke Service B (Payment Service) via OpenFeign
            log.info("[SAGA STEP 1] Sending payment request to Service B [OrderId: {}]", order.getId());

            PaymentRequest paymentRequest = new PaymentRequest(
                    order.getId(),
                    order.getOrderNumber(),
                    order.getCustomerId(),
                    order.getAmount()
            );

            // Synchronous OpenFeign invocation with distributed trace propagation
            paymentResponse = paymentClient.processPayment(paymentRequest);

            // Validate payment response
            if (paymentResponse == null || !"SUCCESS".equalsIgnoreCase(paymentResponse.getStatus())) {
                String failureReason = (paymentResponse != null) ? paymentResponse.getReason() : "No response from Payment Service";
                throw new PaymentFailedException("Payment processing failed", failureReason);
            }

            // Step 3 (Happy Path): Payment succeeded
            log.info("[SAGA SUCCESS] Payment processed successfully [PaymentRef: {}, TraceId: {}]",
                    paymentResponse.getPaymentReference(), traceId);

            order.setStatus(OrderStatus.CONFIRMED);
            order.setUpdatedAt(LocalDateTime.now());
            OrderEntity savedOrder = updateOrderStatus(order);

            recordSagaStep(order.getId(), traceId, "PAYMENT_CONFIRMED", "SUCCESS",
                    "Payment confirmed successfully. Reference: " + paymentResponse.getPaymentReference());

            return savedOrder;

        } catch (Exception ex) {
            // =========================================================================
            // Error Catching and Compensating Transactions (Saga Rollback)
            // =========================================================================
            log.error("[SAGA FAILURE] Downstream error occurred! Initiating Saga compensation [OrderId: {}, Error: {}]",
                    order.getId(), ex.getMessage());

            // Step 4: Compensate local order status to CANCELLED_PAYMENT_FAILED
            order.setStatus(OrderStatus.CANCELLED_PAYMENT_FAILED);
            order.setUpdatedAt(LocalDateTime.now());
            updateOrderStatus(order);

            recordSagaStep(order.getId(), traceId, "ORDER_COMPENSATED_LOCAL", "SUCCESS",
                    "Order status compensated to CANCELLED_PAYMENT_FAILED. Error: " + ex.getMessage());

            // Step 5: Trigger compensating refund in downstream Service B if needed
            triggerCompensatingRefund(order, traceId);

            // Re-throw structured exception for API response
            if (ex instanceof PaymentFailedException pfe) {
                throw pfe;
            } else {
                throw new PaymentFailedException("Order processing failed; order was cancelled", ex.getMessage());
            }
        }
    }

    /**
     * Persist order status in an independent transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderEntity updateOrderStatus(OrderEntity order) {
        return orderRepository.save(order);
    }

    /**
     * Trigger downstream compensating reverse/refund transaction in Service B.
     */
    private void triggerCompensatingRefund(OrderEntity order, String traceId) {
        try {
            log.warn("[SAGA COMPENSATION] Sending refund compensation request to Service B [OrderId: {}]", order.getId());
            PaymentRequest refundReq = new PaymentRequest(
                    order.getId(),
                    order.getOrderNumber(),
                    order.getCustomerId(),
                    order.getAmount()
            );

            PaymentResponse refundRes = paymentClient.compensateRefund(refundReq);
            recordSagaStep(order.getId(), traceId, "COMPENSATING_REFUND", "SUCCESS",
                    "Compensating refund completed in Service B. Status: " + refundRes.getStatus());
        } catch (Exception e) {
            log.error("[SAGA COMPENSATION ERROR] Error during compensating refund: {}", e.getMessage());
            recordSagaStep(order.getId(), traceId, "COMPENSATING_REFUND", "FAILED",
                    "Compensating refund failed: " + e.getMessage());
        }
    }

    /**
     * Persist Saga execution step in an isolated transaction (Propagation.REQUIRES_NEW).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSagaStep(Long orderId, String traceId, String step, String status, String message) {
        try {
            SagaLog logEntry = new SagaLog(orderId, traceId, step, status, message);
            sagaLogRepository.save(logEntry);
        } catch (Exception ex) {
            log.warn("Failed to record Saga log: {}", ex.getMessage());
        }
    }
}
