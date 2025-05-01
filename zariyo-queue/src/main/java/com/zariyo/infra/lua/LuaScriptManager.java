package com.zariyo.infra.lua;

import com.zariyo.exception.ErrorCode;
import com.zariyo.exception.LuaScriptException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LuaScriptManager {

    private final Map<String, DefaultRedisScript<Long>> scripts = new HashMap<>();

    @PostConstruct
    public void init() {
        loadScript("enqueue", "lua/enqueue.lua");
        loadScript("moveToOpenSet", "lua/moveToOpenSet.lua");
        loadScript("addToOpenSet", "lua/addToOpenSet.lua");
        loadScript("schedulerLock", "lua/schedulerLock.lua");
    }

    private void loadScript(String key, String path) {
        try {
            Resource resource = new ClassPathResource(path);
            String lua = Files.readString(resource.getFile().toPath());
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(lua);
            script.setResultType(Long.class);
            scripts.put(key, script);
        } catch (IOException e) {
            throw new LuaScriptException(ErrorCode.LUA_ERROR);
        }
    }

    public DefaultRedisScript<Long> getScript(String key) {
        DefaultRedisScript<Long> script = scripts.get(key);
        if (script == null) {
            throw new IllegalArgumentException("스크립트 없음: " + key);
        }
        return script;
    }
}