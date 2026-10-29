package com.microservices.servicea.repository;

import com.microservices.servicea.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for Order database operations in service_a_db.
 */
@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    // Retrieve order by unique order number
    Optional<OrderEntity> findByOrderNumber(String orderNumber);
}
