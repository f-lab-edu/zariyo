package com.zariyo.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zariyo.common.response.ErrorResponse;
import com.zariyo.common.validation.TokenValidation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@RequiredArgsConstructor
public class TokenValidationInterceptor implements HandlerInterceptor {

    private final TokenValidation tokenValidation;
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("X-QUEUE-TOKEN");
        if (token == null || token.isBlank()) {
            tokenNotFound(response);
            return false;
        }

        boolean valid = tokenValidation.validateAndRefresh(token);
        if (!valid) {
            tokenNotFound(response);
            return false;
        }

        return true;
    }

    private void tokenNotFound(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        String json = mapper.writeValueAsString(new ErrorResponse(401, "유효하지 않은 토큰입니다."));
        response.getWriter().write(json);
    }
}
