package com.zariyo.config;

import com.zariyo.infra.message.RedisExpiredEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class RedisMessageConfig {

    @Qualifier("mainRedisTemplate")
    private final StringRedisTemplate mainRedisTemplate;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(mainRedisTemplate.getConnectionFactory()); // 메인 레디스 팩토리 연결
        container.addMessageListener(new RedisExpiredEventListener(mainRedisTemplate), new PatternTopic("__keyevent@*__:expired"));
        return container;
    }
}