package com.fintra.stocktrading.cache.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.cache.CacheStatistics;
import org.springframework.data.redis.cache.CacheStatisticsCollector;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class RedisCacheOperations implements RedisCacheWriter {
    private final RedisCacheWriter delegate;
    private final RedisStatus redisStatus;

    @Override
    public byte[] get(String name, byte[] key) {
        if (!this.redisStatus.isRedisEnabled()) {
            return null;
        }
        return this.delegate.get(name, key);
    }

    @Override
    public void put(String name, byte[] key, byte[] value, Duration ttl) {
        if (this.redisStatus.isRedisEnabled()) {
            this.delegate.put(name, key, value, ttl);
        }
    }

    @Override
    public byte[] putIfAbsent(String name, byte[] key, byte[] value, Duration ttl) {
        if (this.redisStatus.isRedisEnabled()) {
            return this.delegate.putIfAbsent(name, key, value, ttl);
        }
        return null;
    }

    @Override
    public void remove(String name, byte[] key) {
        if (this.redisStatus.isRedisEnabled()) {
            this.delegate.remove(name, key);
        }
    }

    @Override
    public void clean(String name, byte[] pattern) {
        if (this.redisStatus.isRedisEnabled()) {
            this.delegate.clean(name, pattern);
        }
    }

    @Override
    public RedisCacheWriter withStatisticsCollector(CacheStatisticsCollector collector) {
        RedisCacheWriter newDelegate = this.delegate.withStatisticsCollector(collector);
        return new RedisCacheOperations(newDelegate, this.redisStatus);
    }

    @Override
    public CacheStatistics getCacheStatistics(String name) {
        return this.delegate.getCacheStatistics(name);
    }

    @Override
    public void clearStatistics(String name) {
        this.delegate.clearStatistics(name);
    }

    @Override
    public CompletableFuture<byte[]> retrieve(String name, byte[] key, Duration ttl) {
        if (!this.redisStatus.isRedisEnabled()) {
            return CompletableFuture.completedFuture(null);
        }
        try {
            byte[] result = this.get(name, key);
            return CompletableFuture.completedFuture(result);
        } catch (Exception ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    @Override
    public CompletableFuture<Void> store(String name, byte[] key, byte[] value, Duration ttl) {
        if (!this.redisStatus.isRedisEnabled()) {
            return CompletableFuture.completedFuture(null);
        }
        try {
            this.put(name, key, value, ttl);
            return CompletableFuture.completedFuture(null);
        } catch (Exception ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }
}
