package com.zariyo.access.config;

import com.zariyo.access.stream.QueueLifecycleListener;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class RedisListenerConfig {

    @Qualifier("eventRedisTemplate")
    private final StringRedisTemplate eventRedisTemplate;

    private final QueueLifecycleListener queueLifecycleListener;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(eventRedisTemplate.getConnectionFactory());
        container.addMessageListener(queueLifecycleListener, new PatternTopic("queue:started"));
        return container;
    }
}
