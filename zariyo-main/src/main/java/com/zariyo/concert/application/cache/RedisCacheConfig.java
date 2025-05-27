package com.zariyo.concert.application.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zariyo.concert.application.serializer.GzipRedisSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class RedisCacheConfig {

    private final RedisConnectionFactory mainRedisConnectionFactory;
    private final ObjectMapper objectMapper;

    @Bean
    public CacheManager cacheManager() {
        objectMapper.registerModule(new JavaTimeModule());

        Map<String, RedisCacheConfiguration> cacheConfigMap = Arrays.stream(RedisCache.values())
            .collect(Collectors.toMap(
                RedisCache::getCacheName,
                redisCache -> RedisCacheConfiguration.defaultCacheConfig()
                    .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GzipRedisSerializer<>(objectMapper, redisCache.getTypeRef())))
                    .disableCachingNullValues()
                    .entryTtl(redisCache.getExpiredAfterWrite())
                    .computePrefixWith(cacheName -> "cache:" + cacheName + ":")
            ));

        return RedisCacheManager.RedisCacheManagerBuilder
            .fromConnectionFactory(mainRedisConnectionFactory)
            .withInitialCacheConfigurations(cacheConfigMap)
            .build();
    }
}
