package com.zariyo.service;

import com.zariyo.api.dto.QueueDto;
import com.zariyo.exception.ErrorCode;
import com.zariyo.exception.QueueException;
import com.zariyo.infra.QueueRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueRedisRepository queueRedisRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 현재 서버의 RPS를 기준으로 대기열 등록 여부를 판단
     * - RPS 임계값 초과 시 대기열에 등록 후 토큰 반환
     * - 임계값 미만이면 즉시 입장 가능한 OPEN 상태 토큰 반환
     *
     * @return 대기열 상태 및 토큰 정보
     */
    public QueueDto registerToQueue(){
        boolean isThreshold = queueRedisRepository.isOverRpsThreshold();
        String token = UUID.randomUUID().toString();
        if(isThreshold){
            int position = addToQueue(token);
            return QueueDto.waiting(token, position);
        }else {
            return QueueDto.open(token);
        }
    }

    /**
     * 앞 순번 allowCount(10)개 레디스에서 제거 후 반환
     *
     * @param allowCount 10개
     * @return 레디스에서 제거된 10개 토큰
     */
    public List<String> popTopTokens(int allowCount) {
        return queueRedisRepository.popTopTokens(allowCount);
    }

    /**
     * 대기열에 있는 모든 사용자에게 현재 순번 정보를 실시간으로 전송
     */
    public void broadcastQueueStatus(){
        List<String> waitingTokens = queueRedisRepository.findAllTokens();
        log.info("대기 중인 토큰 수: {}", waitingTokens.size());
        for (String token : waitingTokens) {
            try {
                Long position = queueRedisRepository.getPosition(token);
                if (position != null) {
                    QueueDto response = QueueDto.waiting(token, position.intValue() + 1);
                    messagingTemplate.convertAndSend("/sub/queue/" + token, response);
                }
            } catch (Exception e) {
                log.warn("토큰 {} 의 순번 전송 중 오류 발생", token, e);
            }
        }
    }

    /**
     * 사용자를 대기열에 추가하고 순번을 반환
     *
     * @param token 사용자 식별 토큰
     * @return 대기열 내 사용자 순번
     */
    public int addToQueue(String token){
        return queueRedisRepository.addToQueue(token);
    }

    /**
     * 사용자 토큰의 현재 대기열 순번을 조회
     * @param token token 사용자 식별 토큰
     * @return 순번
     */
    public int getPosition(String token) {
        log.info("토큰 위치 조회: {}", token);
        Long position = queueRedisRepository.getPosition(token);
        if (position == null) {
            log.warn("토큰이 대기열에 존재하지 않습니다: {}", token);
            throw new QueueException(ErrorCode.TOKEN_NOT_FOUND);
        }
        return position.intValue() + 1;
    }

    /**
     * 대기열에서 특정 토큰을 제거
     *
     * @param token 사용자 식별 토큰
     */
    public void removeFromQueue(String token) {
        queueRedisRepository.removeFromQueue(token);
    }

    /**
     * 현재 대기열에 등록된 사용자 수를 반환
     *
     * @return 대기열 크기
     */
    public int getQueueSize() {
        return queueRedisRepository.getCurrentQueueSize();
    }
}
