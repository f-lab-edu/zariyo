package com.zariyo.access.stream;

import com.zariyo.access.service.AccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EntryStreamConsumer {

    @Qualifier("eventRedisTemplate")
    private final StringRedisTemplate eventRedisTemplate;

    private final AccessService accessService;

    private static final String STREAM = "open:entry:stream";
    private static final String GROUP = "open-entry-group";
    private static final String CONSUMER = UUID.randomUUID().toString();

    @Async
    public void start(){
        try {
            eventRedisTemplate.opsForStream().createGroup(STREAM, ReadOffset.from("0-0"), GROUP);
        } catch (Exception ignored) {}

        while (true) {
            List<MapRecord<String, Object, Object>> records =
                    eventRedisTemplate.opsForStream().read(
                            Consumer.from(GROUP, CONSUMER),
                            StreamReadOptions.empty().count(10).block(Duration.ofSeconds(2)),
                            StreamOffset.create(STREAM, ReadOffset.lastConsumed())
                    );
            if (records == null || records.isEmpty()) {
                continue;
            }
            for (MapRecord<String, Object, Object> record : records) {
                Map<Object, Object> valueMap = record.getValue();

                if (valueMap.containsKey("tokens")) {
                    List<String> tokens = (List<String>) valueMap.get("tokens");
                    for (String token : tokens) {
                        accessService.queueTokenToMainSet(token);
                    }
                }
                eventRedisTemplate.opsForStream().acknowledge(STREAM, GROUP, record.getId());
            }
        }
    }

}
