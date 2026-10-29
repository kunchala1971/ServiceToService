package com.microservices.servicea.controller;

import com.microservices.servicea.dto.ApiResponse;
import com.microservices.servicea.dto.CreateOrderRequest;
import com.microservices.servicea.entity.OrderEntity;
import com.microservices.servicea.entity.SagaLog;
import com.microservices.servicea.service.OrderService;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Service A (Order Service) REST Controller.
 * Exposes endpoints for order creation, cached queries, and Saga audit logs.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;
    private final Tracer tracer;

    public OrderController(OrderService orderService, Tracer tracer) {
        this.orderService = orderService;
        this.tracer = tracer;
    }

    private String getTraceId() {
        if (tracer != null && tracer.currentSpan() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return "N/A";
    }

    /**
     * Endpoint to create a new order and trigger the distributed Saga orchestration.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderEntity>> createOrder(@RequestBody CreateOrderRequest request) {
        String traceId = getTraceId();
        log.info("API Call: Order creation initiated [Trace ID: {}]", traceId);

        OrderEntity createdOrder = orderService.createOrder(request);

        ApiResponse<OrderEntity> response = ApiResponse.success(
                "Order processed successfully via Saga orchestration.",
                createdOrder,
                traceId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Cached endpoint to retrieve order details by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderEntity>> getOrderById(@PathVariable("id") Long id) {
        String traceId = getTraceId();
        log.info("API Call: Querying order ID={} [Trace ID: {}]", id, traceId);

        OrderEntity order = orderService.getOrderById(id);

        ApiResponse<OrderEntity> response = ApiResponse.success(
                "Order details retrieved successfully.",
                order,
                traceId
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to inspect distributed Saga execution logs.
     */
    @GetMapping("/{id}/saga-logs")
    public ResponseEntity<ApiResponse<List<SagaLog>>> getSagaLogs(@PathVariable("id") Long id) {
        String traceId = getTraceId();
        List<SagaLog> logs = orderService.getSagaLogs(id);

        ApiResponse<List<SagaLog>> response = ApiResponse.success(
                "Saga audit trail records retrieved successfully.",
                logs,
                traceId
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to delete an order and evict it from the cache.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable("id") Long id) {
        String traceId = getTraceId();
        orderService.deleteOrder(id);

        ApiResponse<Void> response = ApiResponse.success(
                "Order and corresponding cache entry successfully removed.",
                null,
                traceId
        );
        return ResponseEntity.ok(response);
    }
}
