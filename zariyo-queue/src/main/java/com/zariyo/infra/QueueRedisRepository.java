package com.zariyo.infra;

import com.zariyo.exception.ErrorCode;
import com.zariyo.exception.LuaScriptException;
import com.zariyo.exception.QueueException;
import com.zariyo.infra.lua.LuaScriptManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class QueueRedisRepository {

    private final StringRedisTemplate mainRedisTemplate;
    private final StringRedisTemplate queueRedisTemplate;

    private final LuaScriptManager luaScriptManager;

    protected static final String MAIN_REDIS_KEY = "main:";
    protected static final String MAIN_COUNTER_KEY = "main:current";
    protected static final String MAIN_TOKEN_SET_KEY = "mainUsers";
    protected static final String MAIN_THRESHOLD_KEY = "threshold";
    protected static final String QUEUE_REDIS_KEY = "queue";
    protected static final String QUEUE_PUSH_COUNT = "push";
    protected static final String QUEUE_EXIT_COUNT = "exit";

    /**
     * 입장 가능 여부 체크 후 MAIN Redis에 추가
     * (Lua로 원자성 보장)
     * @param token 발급 받은 토큰
     * @return true = 입장성공 / false = 대기상태
     */
    public boolean addToOpenSet(String token) {
        long currentTime = System.currentTimeMillis();
        return Optional.ofNullable(
                mainRedisTemplate.execute(
                        luaScriptManager.getScript("addToOpenSet", Boolean.class),
                        List.of(
                                MAIN_REDIS_KEY + token,
                                MAIN_COUNTER_KEY,
                                MAIN_THRESHOLD_KEY,
                                MAIN_TOKEN_SET_KEY
                        ),
                        String.valueOf(currentTime),
                        token
                )
        ).orElseThrow(() -> new LuaScriptException(ErrorCode.LUA_ERROR));
    }

    /**
     * 대기열에 토큰 추가
     * @param token 발급 받은 토큰
     * @return 대기열에 추가된 순번
     */
    public int enqueue(String token) {
        return Optional.ofNullable(
                        queueRedisTemplate.execute(
                                luaScriptManager.getScript("enqueue", Long.class),
                                List.of(QUEUE_REDIS_KEY, QUEUE_PUSH_COUNT),
                                token
                        )
                ).orElseThrow(() -> new QueueException(ErrorCode.LUA_ERROR)).intValue();
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
}