package com.zariyo.websocket;

import com.zariyo.api.dto.QueueDto;
import com.zariyo.exception.ErrorMessage;
import com.zariyo.exception.QueueException;
import com.zariyo.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class QueueWebSocketHandler {

    private final QueueService queueService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/queue/subscribe")
    public void subscribeToQueue(@Payload String token) {
        log.info("구독 요청 수신: {}", token);
        QueueDto response = QueueDto.waiting(token, queueService.getPosition(token));
        messagingTemplate.convertAndSend("/sub/queue/" + token, response);
    }

    @MessageExceptionHandler(QueueException.class)
    @SendToUser("/queue/errors")
    public ErrorMessage handleQueueException(QueueException e) {
        return ErrorMessage.withErrorCode(e.getErrorCode());
    }
}
