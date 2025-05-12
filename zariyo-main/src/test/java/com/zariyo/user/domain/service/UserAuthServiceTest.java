package com.zariyo.user.domain.service;

import com.zariyo.common.exception.custom.LoginFailedException;
import com.zariyo.user.api.dto.UserDto;
import com.zariyo.user.domain.entity.User;
import com.zariyo.user.infra.UserJpaRepository;
import com.zariyo.user.infra.UserRedisRepository;
import com.zariyo.user.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAuthServiceTest {

    @Mock
    private UserJpaRepository userJpaRepository;
    @Mock
    private UserRedisRepository userRedisRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserAuthService userAuthService;

    @Test
    @DisplayName("로그인 성공 테스트")
    void login_Success() {
        // given
        User loginRequest = User.builder().email("test@example.com").password("testPassword").build();
        User savedUser = User.builder().userId(1L).email("test@example.com").password("testPassword").nickName("nick").build();

        when(userJpaRepository.findByEmail("test@example.com")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("testPassword", "testPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("test@example.com")).thenReturn("jwt-token");

        // when
        UserDto result = userAuthService.login(loginRequest);

        // then
        assertEquals("jwt-token", result.getToken());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    @DisplayName("로그인 실패 - 사용자 없음")
    void login_Fail_UserNotFound() {
        when(userJpaRepository.findByEmail("nope@example.com")).thenReturn(Optional.empty());

        User loginRequest = User.builder().email("nope@example.com").password("pw").build();

        assertThrows(LoginFailedException.class, () -> userAuthService.login(loginRequest));
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_Fail_InvalidPassword() {
        User savedUser = User.builder().email("test@example.com").password("testPassword").build();

        when(userJpaRepository.findByEmail("test@example.com")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("testPassword", "testPassword")).thenReturn(false);

        User loginRequest = User.builder().email("test@example.com").password("testPassword").build();

        assertThrows(LoginFailedException.class, () -> userAuthService.login(loginRequest));
    }

    @Test
    @DisplayName("로그아웃 성공 테스트")
    void logout_Success() {
        // given
        String jwtToken = "test.jwt.token";
        String queueToken = "queue-token";
        Claims claims = mock(Claims.class);
        long exp = System.currentTimeMillis() + 60000;

        when(jwtTokenProvider.getClaims(jwtToken)).thenReturn(claims);
        when(claims.getExpiration()).thenReturn(new Date(exp));

        // when
        userAuthService.logout(jwtToken, queueToken);

        // then
        verify(userRedisRepository).expireToken(eq(jwtToken), eq(queueToken), anyLong());
    }
}
