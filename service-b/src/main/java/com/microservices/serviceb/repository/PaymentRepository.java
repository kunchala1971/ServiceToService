package com.microservices.serviceb.repository;

import com.microservices.serviceb.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for Payment database operations in service_b_db.
 */
@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    // Find payment record by Order ID
    Optional<PaymentEntity> findByOrderId(Long orderId);

    // Find payment record by payment reference code
    Optional<PaymentEntity> findByPaymentReference(String paymentReference);
}
