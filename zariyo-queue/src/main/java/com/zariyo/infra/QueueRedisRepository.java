package com.zariyo.infra;

import com.zariyo.exception.ErrorCode;
import com.zariyo.exception.LuaScriptException;
import com.zariyo.exception.QueueException;
import com.zariyo.infra.lua.LuaScriptManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


@Repository
@RequiredArgsConstructor
public class QueueRedisRepository {
    @Qualifier("mainRedisTemplate")
    private final StringRedisTemplate mainRedisTemplate;

    @Qualifier("queueRedisTemplate")
    private final StringRedisTemplate queueRedisTemplate;

    @Qualifier("lockRedisTemplate")
    private final StringRedisTemplate lockRedisTemplate;

    private final LuaScriptManager luaScriptManager;

    public static final String MAIN_REDIS_KEY = "main:";
    public static final String MAIN_COUNTER_KEY = "main:current";
    private static final String MAIN_THRESHOLD_KEY = "threshold:";
    private static final String QUEUE_REDIS_KEY = "queue:";
    private static final String QUEUE_PUSH_COUNT = "push:";
    private static final String QUEUE_EXIT_COUNT = "exit:";

    /**
     * 메인 입장 최대 허용 인원 조회
     * @return 현재 허용 인원 / 디폴트(500)
     */
    public int getMainThreshold() {
        return Integer.parseInt(
                Optional.ofNullable(mainRedisTemplate.opsForValue().get(MAIN_THRESHOLD_KEY))
                        .orElse("500")
        );
    }

    /**
     * 현재 메인에 접속중인 유저 수
     * @return 접속중인 유저수 / 디폴트(0)
     */
    public int getCurrentMainUserCount() {
        return Integer.parseInt(
                Optional.ofNullable(mainRedisTemplate.opsForValue().get(MAIN_COUNTER_KEY))
                        .orElse("0")
        );
    }

    /**
     * 입장 가능 여부 체크 후 MAIN Redis에 추가
     * (Lua로 원자성 보장)
     * @param token 발급 받은 토큰
     * @return true = 입장성공 / false = 대기상태
     */
    public boolean addToOpenSet(String token) {
        Long result = Optional.ofNullable(
                mainRedisTemplate.execute(
                        luaScriptManager.getScript("addToOpenSet"),
                        List.of(MAIN_REDIS_KEY + token, MAIN_COUNTER_KEY, MAIN_THRESHOLD_KEY),
                        "10"
                )
        ).orElseThrow(() -> new LuaScriptException(ErrorCode.LUA_ERROR));
        return result == 1L;
    }

    /**
     * 대기열에 토큰 추가
     * @param token 발급 받은 토큰
     * @return 대기열에 추가된 순번
     */
    public int enqueue(String token) {
        return Optional.ofNullable(
                        queueRedisTemplate.execute(
                                luaScriptManager.getScript("enqueue"),
                                List.of(QUEUE_REDIS_KEY, QUEUE_PUSH_COUNT),
                                token
                        )
                ).map(Long::intValue)
                .orElseThrow(() -> new QueueException(ErrorCode.LUA_ERROR));
    }

    /**
     * 대기열에서 입장토큰 저장 영역으로 이동
     * @param currentOpenCount 이동할 인원 수
     * @return 현재 남은 대기열 인원 수
     */
    public int moveToOpenSet(int currentOpenCount) {
        List<String> tokens = queueRedisTemplate.opsForList().leftPop(QUEUE_REDIS_KEY, currentOpenCount);
        if (tokens == null || tokens.isEmpty()) {
            return 0;
        }

        List<String> keys = new ArrayList<>();
        for (String token : tokens) {
            keys.add(MAIN_REDIS_KEY + token);
        }
        keys.add(MAIN_COUNTER_KEY);

        Long move = Optional.ofNullable(
                mainRedisTemplate.execute(
                        luaScriptManager.getScript("moveToOpenSet"),
                        keys,
                        "1"
                )
        ).orElseThrow(() -> new LuaScriptException(ErrorCode.LUA_ERROR));

        queueRedisTemplate.opsForValue().increment(QUEUE_EXIT_COUNT);
        return Optional.ofNullable(queueRedisTemplate.opsForList().size(QUEUE_REDIS_KEY)).orElse(0L).intValue();
    }

    /**
     * 토큰 입장 상태 조회
     * (main Redis에 토큰 존재 여부)
     * @param token 토큰
     * @return true = 입장 가능, false = 대기 중
     */
    public boolean getQueueStatus(String token) {
        return Boolean.TRUE.equals(mainRedisTemplate.hasKey(MAIN_REDIS_KEY + token));
    }

    /**
     * 대기열에서 나간 인원 수 조회
     * @return 나간 인원 수
     */
    public int getQueueExitCount() {
        return Integer.parseInt(
                Optional.ofNullable(queueRedisTemplate.opsForValue().get(QUEUE_EXIT_COUNT))
                        .orElse("0")
        );
    }

    /**
     * 토큰 TTL 갱신
     * (폴링시 TTL 갱신 처리)
     * @param token 토큰
     */

    public void refreshTokenTTL(String token) {
        mainRedisTemplate.expire(MAIN_REDIS_KEY + token, 10, TimeUnit.SECONDS);
    }

    /**
     * 락 획득 시도
     * (Lua 원자성, TTL 설정)
     * @param key 락 키
     * @param ttlSeconds 락 유지 시간
     * @return true = 락 획득 성공
     */
    public boolean tryAcquireLock(String key, int ttlSeconds) {
        DefaultRedisScript<Long> script = luaScriptManager.getScript("schedulerLock");
        Long checkValue = lockRedisTemplate.execute(
                script,
                List.of(key),
                "vary",
                String.valueOf(ttlSeconds)
        );
        return checkValue != null && checkValue == 1L;
    }


    /**
     * 락 TTL 갱신
     * @param key 락 키
     * @param ttlSeconds 새 TTL
     */
    public void refreshLock(String key, int ttlSeconds) {
        lockRedisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
    }

    /**
     * 락 해제
     * @param key 락 키
     */
    public void releaseLock(String key) {
        lockRedisTemplate.delete(key);
    }
}