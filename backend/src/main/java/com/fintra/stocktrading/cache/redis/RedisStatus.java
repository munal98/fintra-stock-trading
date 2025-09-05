package com.fintra.stocktrading.cache.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.data.redis.RedisHealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@ConditionalOnProperty(name = "api.redis.enabled", havingValue = "true")
@RequiredArgsConstructor
public class RedisStatus {

    private final AtomicBoolean redisEnabled = new AtomicBoolean(true);
    private final RedisHealthIndicator redisHealthIndicator;

    @Scheduled(
            fixedDelay   = 15,
            initialDelay = 15,
            timeUnit = TimeUnit.SECONDS
    )
    public void checkRedisStatus() {
        this.redisEnabled.set(isRedisUp());
    }

    public boolean isRedisEnabled() {
        return this.redisEnabled.get();
    }

    public boolean isRedisUp() {
        return Status.UP.equals(this.redisHealthIndicator.health().getStatus());
    }

}
