package com.fintra.stocktrading.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

@Slf4j
public class LogCacheErrorHandler implements CacheErrorHandler {

    /**
     * @param k
     */
    @Override
    public void handleCacheGetError(RuntimeException ex, Cache c, Object k) {
        warn("GET", ex, c);
    }

    /**
     * @param k
     * @param v
     */
    @Override
    public void handleCachePutError(RuntimeException ex, Cache c, Object k, Object v) {
        warn("PUT", ex, c);
    }

    /**
     * @param k
     */
    @Override
    public void handleCacheEvictError(RuntimeException ex, Cache c, Object k) {
        warn("EVICT", ex, c);
    }

    @Override
    public void handleCacheClearError(RuntimeException ex, Cache c) {
        warn("CLEAR", ex, c);
    }

    private static void warn(String op, RuntimeException ex, Cache c) {
        log.warn("[CACHE-{}] {} Error: {}", op, c.getName(), ex.getMessage());
    }
}
