package com.microservices.serviceb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Service B (Payment Microservice) Main Application Class.
 * Runs on port 8082 and connects to service_b_db.
 */
@SpringBootApplication
public class ServiceBApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceBApplication.class, args);
        System.out.println("==========================================================");
        System.out.println(" Service B (Payment Service) successfully started on port: 8082");
        System.out.println(" Connected Database: service_b_db");
        System.out.println("==========================================================");
    }
}
