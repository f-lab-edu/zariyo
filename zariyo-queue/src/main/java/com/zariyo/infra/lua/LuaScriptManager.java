package com.zariyo.infra.lua;

import com.zariyo.exception.ErrorCode;
import com.zariyo.exception.LuaScriptException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LuaScriptManager {

    private final Map<String, DefaultRedisScript<?>> scripts = new HashMap<>();

    @PostConstruct
    public void init() {
        loadScript("enqueue", "lua/enqueue.lua", Long.class);
        loadScript("popAndIncrement", "lua/popAndIncrement.lua", String.class);
        loadScript("clearIfEmpty", "lua/clearIfEmpty.lua", Boolean.class);
    }

    private <T> void loadScript(String key, String path, Class<T> resultType) {
        try {
            Resource resource = new ClassPathResource(path);
            String lua = Files.readString(resource.getFile().toPath());
            DefaultRedisScript<T> script = new DefaultRedisScript<>();
            script.setScriptText(lua);
            script.setResultType(resultType);
            scripts.put(key, script);
        } catch (IOException e) {
            throw new LuaScriptException(ErrorCode.LUA_ERROR);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> DefaultRedisScript<T> getScript(String key, Class<T> resultType) {
        DefaultRedisScript<T> script = (DefaultRedisScript<T>) scripts.get(key);
        if (script == null) {
            log.error("Script {} not found", key);
            throw new LuaScriptException(ErrorCode.LUA_ERROR);
        }
        return script;
    }
}