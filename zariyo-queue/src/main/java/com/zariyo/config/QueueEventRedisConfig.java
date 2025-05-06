package com.zariyo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class QueueEventRedisConfig {

    @Value("${redis.event.host}")
    private String redisHost;

    @Value("${redis.event.port}")
    private int redisPort;

    @Bean(name="queueEventRedisTemplate")
    public StringRedisTemplate eventRedisTemplate() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet();
        return new StringRedisTemplate(factory);
    }
}
