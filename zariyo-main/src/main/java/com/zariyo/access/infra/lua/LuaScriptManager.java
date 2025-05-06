package com.zariyo.access.infra.lua;

import com.zariyo.common.exception.ErrorCode;
import com.zariyo.common.exception.custom.LuaScriptException;
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

    private final Map<String, DefaultRedisScript<Boolean>> scripts = new HashMap<>();

    @PostConstruct
    public void init() {
        loadScript("addToOpenSet", "lua/addToOpenSet.lua");
        loadScript("queueTokenToMainSet", "lua/queueTokenToMainSet.lua");
    }

    private void loadScript(String key, String path) {
        try {
            Resource resource = new ClassPathResource(path);
            String lua = Files.readString(resource.getFile().toPath());
            DefaultRedisScript<Boolean> script = new DefaultRedisScript<>();
            script.setScriptText(lua);
            script.setResultType(Boolean.class);
            scripts.put(key, script);
        } catch (IOException e) {
            throw new LuaScriptException(ErrorCode.LUA_ERROR);
        }
    }

    public DefaultRedisScript<Boolean> getScript(String key) {
        DefaultRedisScript<Boolean> script = scripts.get(key);
        if (script == null) {
            log.error("Script {} not found", key);
            throw new LuaScriptException(ErrorCode.LUA_ERROR);
        }
        return script;
    }
}