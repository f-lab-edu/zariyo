package com.zariyo.user.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zariyo.common.response.ErrorResponse;
import com.zariyo.user.security.validation.TokenValidation;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenValidation tokenValidation;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String bearer = request.getHeader("Authorization");
        String token = null;

        if (!uri.startsWith("/reserve")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            token = bearer.substring(7);
        }

        if (token == null || token.isBlank()) {
            sendJwtError(response, "JWT_TOKEN_MISSING", "JWT 토큰이 존재하지 않습니다.");
            return;
        }

        if (Boolean.TRUE.equals(tokenValidation.isBlacklisted(token))) {
            sendJwtError(response, "BLACKLISTED", "블랙리스트에 등록된 토큰입니다.");
            return;
        }

        try {
            Claims claims = jwtTokenProvider.getClaims(token);
            String email = claims.getSubject();
            Long userId = claims.get("userId", Long.class);

            CustomUserDetails userDetails = new CustomUserDetails(userId, email);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            if (isTokenExpiringSoon(claims)) {
                String newToken = jwtTokenProvider.generateToken(claims.getSubject(), userId);
                long remaining = claims.getExpiration().getTime() - System.currentTimeMillis();
                tokenValidation.saveBlacklistToken(token, remaining);
                response.setHeader("X-NEW-TOKEN", newToken);
            }

        } catch (JwtException e) {
            sendJwtError(response, "INVALID_TOKEN", "유효하지 않은 토큰입니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isTokenExpiringSoon(Claims claims) {
        long exp = claims.getExpiration().getTime();
        long now = System.currentTimeMillis();
        return (exp - now) <= 5 * 60 * 1000; // 5분
    }

    private void sendJwtError(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        response.getWriter().write(objectMapper.writeValueAsString(ErrorResponse.of(code, message)));
    }
}
