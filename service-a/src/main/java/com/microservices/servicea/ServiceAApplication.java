package com.microservices.servicea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Service A (Order Microservice) Main Application Class.
 * 
 * 1. @SpringBootApplication: Enables auto-configuration and component scanning.
 * 2. @EnableFeignClients: Scans and registers declarative OpenFeign clients for inter-service communication.
 */
@SpringBootApplication
@EnableFeignClients
public class ServiceAApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceAApplication.class, args);
        System.out.println("==========================================================");
        System.out.println(" Service A (Order Service) successfully started on port: 8081");
        System.out.println(" Connected Database: service_a_db");
        System.out.println("==========================================================");
    }
}
