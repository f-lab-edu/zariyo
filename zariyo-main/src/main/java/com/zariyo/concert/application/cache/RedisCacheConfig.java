package com.zariyo.concert.application.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zariyo.concert.application.serializer.GzipRedisSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class RedisCacheConfig {

    private final RedisConnectionFactory mainRedisConnectionFactory;
    private final ObjectMapper objectMapper;

    @Bean(name = "redisCacheManager")
    public CacheManager redisCacheManager() {
        objectMapper.registerModule(new JavaTimeModule());

        Map<String, RedisCacheConfiguration> cacheConfigMap = Arrays.stream(RedisCache.values())
            .filter(redisCache -> "redis".equals(redisCache.getCacheStore()))
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

    @Bean(name = "localCacheManager")
    @Primary
    public CacheManager localCacheManager() {
        List<CaffeineCache> caffeineCaches = Arrays.stream(RedisCache.values())
                .filter(redisCache -> "local".equals(redisCache.getCacheStore()))
                .map(cache -> new CaffeineCache(
                        cache.getCacheName(),
                        Caffeine.newBuilder()
                                .expireAfterWrite(cache.getExpiredAfterWrite())
                                .maximumSize(1000)
                                .build()
                ))
                .toList();

        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(new ArrayList<>(caffeineCaches));
        return cacheManager;
    }
}
