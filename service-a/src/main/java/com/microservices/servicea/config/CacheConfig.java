package com.microservices.servicea.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Caching Configuration Class.
 * 
 * @EnableCaching: Enables Spring Boot's caching infrastructure.
 * Uses ConcurrentMapCacheManager (In-Memory Cache).
 * In production environments, this can seamlessly be backed by Redis or Caffeine.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Cache Manager bean creating the 'orders' cache region.
     * Frequently accessed orders are served directly from fast in-memory cache
     * rather than performing repetitive MySQL queries.
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager("orders");
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }
}
