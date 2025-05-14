package com.zariyo.user.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zariyo.common.response.ErrorResponse;
import com.zariyo.user.security.validation.TokenValidation;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class QueueTokenValidationFilter extends OncePerRequestFilter {

    private final TokenValidation tokenValidation;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String queueToken = request.getHeader("X-QUEUE-TOKEN");

        // Queue 토큰이 없는 경우
        if (queueToken == null || queueToken.isBlank()) {
            sendQueueTokenError(response, "QUEUE_TOKEN_REQUIRED", "대기열 토큰이 필요합니다.");
            return;
        }

        // Queue 토큰 유효성 검증
        if (!tokenValidation.validateAndRefresh(queueToken)) {
            sendQueueTokenError(response, "INVALID_QUEUE_TOKEN", "유효하지 않은 대기열 토큰입니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendQueueTokenError(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        response.getWriter().write(objectMapper.writeValueAsString(ErrorResponse.withRedirect(code, message, "/queue/token")));
    }
}
