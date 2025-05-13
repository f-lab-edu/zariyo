package com.zariyo.service;

import com.zariyo.api.dto.QueueDto;
import com.zariyo.infra.QueueRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueRedisRepository queueRedisRepository;

    /**
     * 바로입장 or 대기열
     */
    public QueueDto enterOrEnqueue() {
        String token = UUID.randomUUID().toString();
        if(queueRedisRepository.addToOpenSet(token)){
            return QueueDto.open(token);
        }
        int queueEntryNumber = queueRedisRepository.enqueue(token);
        int position = queueEntryNumber - queueRedisRepository.getQueueExitCount();
        return QueueDto.waiting(token, queueEntryNumber, position);
    }

    /**
     * 현재 내 순번 조회 및 입장 가능 여부 확인
     *
     * @param token 발급받은 토큰
     * @param entryNumber 대기열 진입 순번
     * @return 입장 가능 상태(Open) 또는 대기 상태(Waiting)
     */
    public QueueDto getQueueStatusAndPosition(String token, int entryNumber) {
        if(queueRedisRepository.getQueueStatus(token)){
            return QueueDto.open(token);
        } else {
            int position = entryNumber - queueRedisRepository.getQueueExitCount();
            return QueueDto.waiting(token, entryNumber,position);
        }
    }
}