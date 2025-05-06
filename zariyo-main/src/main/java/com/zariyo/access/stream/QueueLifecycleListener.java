package com.zariyo.access.stream;

import com.zariyo.access.infra.task.QueueOpenMonitor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class QueueLifecycleListener implements MessageListener {

    private final QueueOpenMonitor queueOpenMonitor;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String msg = new String(message.getBody(), StandardCharsets.UTF_8);
        if(msg.equals("start")){
            queueOpenMonitor.startOpenMonitoring();
        } else if(msg.equals("stop")){
            queueOpenMonitor.stopOpenMonitoring();
        }
    }
}
