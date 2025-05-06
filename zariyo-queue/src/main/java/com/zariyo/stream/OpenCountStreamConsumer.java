package com.zariyo.stream;

import com.zariyo.service.QueueService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OpenCountStreamConsumer {

    @Qualifier("queueEventRedisTemplate")
    private final StringRedisTemplate queueEventRedisTemplate;

    private final QueueService queueService;

    private static final String STREAM = "open:count:stream";
    private static final String ENTRY_STREAM = "open:entry:stream";
    private static final String GROUP = "open-count-group";
    private static final String CONSUMER = UUID.randomUUID().toString();

    @PostConstruct
    @Async
    public void listenForOpenCounts() {
        try {
            queueEventRedisTemplate.opsForStream().createGroup(STREAM, GROUP);
        } catch (Exception ignored) {
        }

        while (true) {
            List<MapRecord<String, Object, Object>> records = queueEventRedisTemplate.opsForStream().read(
                    Consumer.from(GROUP, CONSUMER),
                    StreamReadOptions.empty().count(1).block(Duration.ofSeconds(1)),
                    StreamOffset.create(STREAM, ReadOffset.lastConsumed())
            );

            if (records == null || records.isEmpty()) continue;

            for (MapRecord<String, Object, Object> record : records) {
                int openCount = Integer.parseInt(record.getValue().get("count").toString());
                List<String> tokens = queueService.popOpenTokens(openCount);

                if (tokens != null && !tokens.isEmpty()) {
                    queueEventRedisTemplate.execute((RedisCallback<Object>) connection -> {
                        connection.openPipeline();
                        for (String token : tokens) {
                            connection.streamCommands().xAdd(
                                    ENTRY_STREAM.getBytes(StandardCharsets.UTF_8),
                                    Map.of("token".getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8))
                            );
                        }
                        connection.closePipeline();
                        return null;
                    });

                    if (queueService.clearIfQueueIsEmpty()) {
                        queueEventRedisTemplate.convertAndSend("queue:started", "stop");
                    }
                } else {
                    queueEventRedisTemplate.convertAndSend("queue:started", "stop");
                }

                queueEventRedisTemplate.opsForStream().acknowledge(STREAM, GROUP, record.getId());
            }
        }
    }
}