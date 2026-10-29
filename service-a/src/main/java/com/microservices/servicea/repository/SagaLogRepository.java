package com.microservices.servicea.repository;

import com.microservices.servicea.entity.SagaLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for persisting and querying Saga execution logs.
 */
@Repository
public interface SagaLogRepository extends JpaRepository<SagaLog, Long> {

    // Retrieve all Saga logs for a specific order sorted chronologically
    List<SagaLog> findByOrderIdOrderByTimestampAsc(Long orderId);
}
