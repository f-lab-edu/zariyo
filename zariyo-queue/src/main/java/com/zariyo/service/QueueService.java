package com.zariyo.service;

import com.zariyo.api.dto.QueueDto;
import com.zariyo.infra.QueueRedisRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QueueService {

    @Qualifier("queueEventRedisTemplate")
    private final StringRedisTemplate queueEventRedisTemplate;

    private final QueueRedisRepository queueRedisRepository;

    /**
     * 대기열에 토크 추가
     */
    public QueueDto enqueueToken(String token) {
        int entryNumber = queueRedisRepository.enqueue(token);
        int exitCount = queueRedisRepository.getCurrentExitCount();
        int position = entryNumber - exitCount;

        // 대기열 최초 생성
        if (entryNumber == 1) {
            queueEventRedisTemplate.convertAndSend("queue:started", "start");
        }
        return QueueDto.waiting(token, entryNumber, position);
    }

    public List<String> popOpenTokens(int openCount) {
        return queueRedisRepository.popQueueToken(openCount);
    }

    public QueueDto getcheckQueueMyPosition(int entryNumber) {
        int exitCount = queueRedisRepository.getCurrentExitCount();
        int position = entryNumber - exitCount;
        return QueueDto.currentPosition(entryNumber, position);
    }

    public boolean clearIfQueueIsEmpty() {
        return queueRedisRepository.clearIfQueueIsEmpty();
    }

}