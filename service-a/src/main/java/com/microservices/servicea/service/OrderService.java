package com.microservices.servicea.service;

import com.microservices.servicea.dto.CreateOrderRequest;
import com.microservices.servicea.entity.OrderEntity;
import com.microservices.servicea.entity.OrderStatus;
import com.microservices.servicea.entity.SagaLog;
import com.microservices.servicea.exception.OrderNotFoundException;
import com.microservices.servicea.repository.OrderRepository;
import com.microservices.servicea.repository.SagaLogRepository;
import com.microservices.servicea.saga.OrderSagaOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Order Business Service.
 * 
 * Demonstrates Caching Techniques and Saga Orchestration:
 * 1. @Cacheable: Caches query results to eliminate redundant database reads.
 * 2. @CachePut: Updates cache entries whenever order data is mutated.
 * 3. @CacheEvict: Invalidates stale cached items upon deletion or cancellation.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final SagaLogRepository sagaLogRepository;
    private final OrderSagaOrchestrator sagaOrchestrator;

    public OrderService(OrderRepository orderRepository,
                        SagaLogRepository sagaLogRepository,
                        OrderSagaOrchestrator sagaOrchestrator) {
        this.orderRepository = orderRepository;
        this.sagaLogRepository = sagaLogRepository;
        this.sagaOrchestrator = sagaOrchestrator;
    }

    /**
     * Creates a new order in PENDING status and delegates to the Saga Orchestrator.
     */
    public OrderEntity createOrder(CreateOrderRequest request) {
        log.info("Received new order request: CustomerId={}, Amount={}", request.getCustomerId(), request.getAmount());

        // Generate unique tracking order number
        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Persist initial order with PENDING status in local service_a_db
        OrderEntity newOrder = new OrderEntity(
                orderNumber,
                request.getCustomerId(),
                request.getProductId(),
                request.getAmount(),
                OrderStatus.PENDING
        );

        OrderEntity savedOrder = orderRepository.save(newOrder);

        // Execute distributed Saga workflow
        return sagaOrchestrator.executeOrderSaga(savedOrder);
    }

    /**
     * Caching Technique 1: @Cacheable
     * 
     * The first invocation retrieves the order from MySQL (service_a_db) and caches it.
     * Subsequent calls return the cached order instance in memory immediately without hitting MySQL.
     */
    @Cacheable(value = "orders", key = "#id")
    @Transactional(readOnly = true)
    public OrderEntity getOrderById(Long id) {
        log.info("[CACHE MISS] Fetching order ID={} from database (service_a_db)...", id);
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order with ID " + id + " was not found."));
    }

    /**
     * Caching Technique 2: @CachePut
     * 
     * Refreshes the cache entry with the newly updated entity state.
     */
    @CachePut(value = "orders", key = "#result.id")
    @Transactional
    public OrderEntity updateOrder(OrderEntity order) {
        log.info("[CACHE UPDATE] Updating order ID={} and refreshing cache entry.", order.getId());
        return orderRepository.save(order);
    }

    /**
     * Caching Technique 3: @CacheEvict
     * 
     * Removes the item from cache when deleted or cancelled to prevent stale reads.
     */
    @CacheEvict(value = "orders", key = "#id")
    @Transactional
    public void deleteOrder(Long id) {
        log.info("[CACHE EVICT] Evicting order ID={} from cache and removing from database.", id);
        OrderEntity order = getOrderById(id);
        orderRepository.delete(order);
    }

    /**
     * Retrieve Saga execution logs for audit and verification.
     */
    @Transactional(readOnly = true)
    public List<SagaLog> getSagaLogs(Long orderId) {
        return sagaLogRepository.findByOrderIdOrderByTimestampAsc(orderId);
    }
}
