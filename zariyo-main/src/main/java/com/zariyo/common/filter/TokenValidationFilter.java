package com.zariyo.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class TokenValidationFilter extends OncePerRequestFilter {

    private final StringRedisTemplate mainRedisTemplate;
    private final List<HandlerMapping> handlerMappings;

    private static final String MAIN_REDIS_KEY = "main:";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // 토큰 발급, 상태 조회 및 Threshold 설정은 필터 제외
        if (path.equals("/access/token") || path.equals("/access/status")) {
            return true;
        }
        try {
            for (HandlerMapping mapping : handlerMappings) {
                HandlerExecutionChain chain = mapping.getHandler(request);
                if (chain != null) return false;
            }
        } catch (Exception ignored) {}
        return true;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("필터 통과 - {} {}", request.getMethod(), request.getRequestURI());
        String token = request.getHeader("X-ACCESS-TOKEN");
        if (StringUtils.hasText(token)) {
            String key = MAIN_REDIS_KEY + token;
            Boolean exists = mainRedisTemplate.hasKey(key);
            if (Boolean.TRUE.equals(exists)) {
                mainRedisTemplate.expire(key, Duration.ofMinutes(10));
            } else {
                response.sendRedirect("/access/token");
                return;
            }
        } else {
            response.sendRedirect("/access/token");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
