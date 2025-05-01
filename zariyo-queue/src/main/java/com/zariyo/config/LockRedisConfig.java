package com.zariyo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class LockRedisConfig {

    @Value("${redis.lock.host}")
    private String lockHost;

    @Value("${redis.lock.port}")
    private int lockPort;

    @Bean(name = "lockRedisTemplate")
    public StringRedisTemplate queueRedisTemplate() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(lockHost, lockPort);
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet();
        return new StringRedisTemplate(factory);
    }
}