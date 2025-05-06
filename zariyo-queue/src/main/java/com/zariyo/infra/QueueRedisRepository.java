package com.zariyo.infra;

import com.zariyo.exception.ErrorCode;
import com.zariyo.exception.LuaScriptException;
import com.zariyo.infra.lua.LuaScriptManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class QueueRedisRepository {

    @Qualifier("queueRedisTemplate")
    private final StringRedisTemplate queueRedisTemplate;

    private final LuaScriptManager luaScriptManager;

    private static final String QUEUE_REDIS_KEY = "queue";
    private static final String QUEUE_PUSH_COUNT = "queue:push";
    private static final String QUEUE_EXIT_COUNT = "queue:exit";


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
        ).map(Long::intValue).orElseThrow(()-> new LuaScriptException(ErrorCode.LUA_ERROR));
    }

    public int getCurrentExitCount() {
        return Integer.parseInt(
                Optional.ofNullable(queueRedisTemplate.opsForValue().get(QUEUE_EXIT_COUNT))
                        .orElse("0")
        );
    }

    @SuppressWarnings("unchecked")
    public List<String> popQueueToken(int openCount) {
        return queueRedisTemplate.execute(
                luaScriptManager.getScript("popAndIncrement", List.class),
                List.of(QUEUE_REDIS_KEY, QUEUE_EXIT_COUNT),
                String.valueOf(openCount)
        );
    }

    public boolean clearIfQueueIsEmpty() {
        Boolean result = queueRedisTemplate.execute(
                luaScriptManager.getScript("clearIfEmpty", Boolean.class),
                List.of(QUEUE_REDIS_KEY, QUEUE_PUSH_COUNT, QUEUE_EXIT_COUNT)
        );
        return Boolean.TRUE.equals(result);
    }

}