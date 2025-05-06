package com.zariyo.access.infra;

import com.zariyo.access.infra.lua.LuaScriptManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class MainRedisRepository {
    @Qualifier("mainRedisTemplate")
    private final StringRedisTemplate mainRedisTemplate;
    private final LuaScriptManager luaScriptManager;

    private static final String MAIN_REDIS_KEY = "main:";
    private static final String MAIN_COUNTER_KEY = "main:current";
    private static final String MAIN_THRESHOLD_KEY = "main:threshold";


    public Boolean addToOpenSet(String token) {
        return mainRedisTemplate.execute(
                luaScriptManager.getScript("addToOpenSet"),
                List.of(MAIN_REDIS_KEY + token, MAIN_COUNTER_KEY, MAIN_THRESHOLD_KEY),
                "600"
        );
    }

    public void queueTokenToMainSet(String token) {
        mainRedisTemplate.execute(
                luaScriptManager.getScript("queueTokenToMainSet"),
                List.of(MAIN_REDIS_KEY + token, MAIN_COUNTER_KEY),
                "600"
        );
    }

    public String getCurrentStatusToken(String token) {
        return mainRedisTemplate.opsForValue().get(MAIN_REDIS_KEY + token);
    }

}
