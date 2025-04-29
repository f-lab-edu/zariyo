package com.zariyo.service;

import com.zariyo.api.dto.QueueDto;
import com.zariyo.infra.QueueRedisRepository;
import com.zariyo.service.event.QueueStartedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueRedisRepository queueRedisRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final String SCHEDULER_LOCK_KEY = "scheduler-lock";

    /**
     * 메인 입장 최대 허용 인원 조회
     * @return 허용 인원 수 (Threshold)
     */
    public int getMainThreshold(){
        return queueRedisRepository.getMainThreshold();
    }

    /**
     * 현재 메인 입장자 수 조회
     * @return 현재 메인 입장한 유저 수
     */
    public int getCurrentMainUserCount() {
        return queueRedisRepository.getCurrentMainUserCount();
    }

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
     * 현재 메인에 입장가능한 인원 수 만큼 대기열 LIST에서 토큰을 메인 SET으로 이동
     * @param currentOpenCount 현재 입장가능한 인원
     * @return 이동 후 남은 대기열 사이즈
     */
    public int popTokens(int currentOpenCount) {
        return queueRedisRepository.moveToOpenSet(currentOpenCount);
    }

    /**
     * 현재 내 순번 조회 및 입장 가능 여부 확인
     * 1. 대기열 진입 순번(entryNumber) - 현재까지 입장 완료된 인원 수 = 현재 내 순번
     * 2. 비동기 이벤트를 발행해서 스케줄러를 시도
     *
     * @param token 발급받은 토큰
     * @param entryNumber 대기열 진입 순번
     * @return 입장 가능 상태(Open) 또는 대기 상태(Waiting)
     */
    public QueueDto getQueuePosition(String token, int entryNumber) {
        eventPublisher.publishEvent(new QueueStartedEvent(token));
        if(queueRedisRepository.getQueueStatus(token)){
            return QueueDto.open(token);
        } else {
            int position = entryNumber - queueRedisRepository.getQueueExitCount();
            return QueueDto.waiting(token, entryNumber,position);
        }
    }

    /**
     * 현재 접속중인 유저 토큰 TTL 갱신
     * 지속적인 폴링을 통해 중도 이탈자 제거를 목표
     * @param token 갱신할 토큰
     */
    public void refreshTokenTTL(String token) {
        queueRedisRepository.refreshTokenTTL(token);
    }

    /**
     * 스케줄러 락 획득 시도
     * 락을 획득하면 스케줄러를 실행할 수 있음.
     *
     * @return 락 획득 성공 여부
     */
    public boolean checkSchedulerLock() {
        return queueRedisRepository.tryAcquireLock(SCHEDULER_LOCK_KEY, 10);
    }

    /**
     * 스케줄러 락 TTL 갱신
     * 스케줄러 실행 중 서버 다운을 고려해 TTL을 삽입하고
     * 스케줄러 주기마다 TTL을 연장해 서버 장애 대비.
     */
    public void refreshSchedulerLock() {
        queueRedisRepository.refreshLock(SCHEDULER_LOCK_KEY, 10);
    }

    /**
     * 스케줄러 락 해제
     * 스케줄러 종료 시 락 삭제 처리.
     */
    public void releaseSchedulerLock() {
        queueRedisRepository.releaseLock(SCHEDULER_LOCK_KEY);
    }
}