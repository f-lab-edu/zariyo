package com.zariyo.infra.message;

import com.zariyo.infra.QueueRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisExpiredEventListener implements MessageListener {

    private final StringRedisTemplate mainRedisTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        if (expiredKey.startsWith(QueueRedisRepository.MAIN_REDIS_KEY)) {
            mainRedisTemplate.opsForValue().decrement(QueueRedisRepository.MAIN_COUNTER_KEY);
        }
    }
}