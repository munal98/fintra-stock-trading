package com.fintra.stocktrading.config;

import org.springframework.boot.actuate.data.redis.RedisHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@ConditionalOnProperty(name = "api.redis.enabled", havingValue = "true")
public class RedisConfig {

    @Bean
    public RedisHealthIndicator redisHealthIndicator(RedisConnectionFactory cf) {
        return new RedisHealthIndicator(cf);
    }

}
