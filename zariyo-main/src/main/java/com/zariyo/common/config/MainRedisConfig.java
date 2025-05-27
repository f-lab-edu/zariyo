package com.zariyo.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class MainRedisConfig {

    @Value("${redis.main.host}")
    private String redisHost;

    @Value("${redis.main.port}")
    private int redisPort;

    @Bean(name = "mainRedisConnectionFactory")
    public LettuceConnectionFactory mainRedisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(redisHost, redisPort));
    }

    @Bean(name = "mainRedisTemplate")
    public StringRedisTemplate mainRedisTemplate() {
        return new StringRedisTemplate(mainRedisConnectionFactory());
    }
}
