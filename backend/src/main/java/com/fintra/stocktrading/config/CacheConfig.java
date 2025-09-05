package com.fintra.stocktrading.config;

import com.fintra.stocktrading.cache.redis.RedisCacheOperations;
import com.fintra.stocktrading.cache.redis.RedisStatus;
import com.fintra.stocktrading.handler.LogCacheErrorHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@ConditionalOnProperty(name = "api.cache.enabled", havingValue = "true")
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    @Override
    public CacheErrorHandler errorHandler() {
        return new LogCacheErrorHandler();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
    public CacheManager redisCacheManager(
            RedisConnectionFactory redisConnectionFactory,
            RedisStatus redisStatus
    ) {
        if (!redisStatus.isRedisUp()) {
            throw new IllegalStateException("Redis is unreachable at startup!");
        }

        RedisCacheWriter cacheWriter = new RedisCacheOperations(
                RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory),
                redisStatus
        );

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(cacheWriter)
                .cacheDefaults(config)
                .build();
    }

}
