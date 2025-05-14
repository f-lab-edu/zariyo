package com.zariyo.user.domain.service;

import com.zariyo.user.api.dto.UserDto;
import com.zariyo.user.domain.entity.User;
import com.zariyo.user.infra.UserRedisRepository;
import com.zariyo.user.infra.UserJpaRepository;
import com.zariyo.user.security.JwtTokenProvider;
import com.zariyo.common.exception.ErrorCode;
import com.zariyo.common.exception.custom.LoginFailedException;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * UserAuthService 는 로그인과 로그아웃을 처리하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final UserJpaRepository userJpaRepository;
    private final UserRedisRepository userRedisRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 로그인 - 이메일과 비밀번호로 로그인
     * @param user
     * @return UserDto
     */
    public UserDto login(User user) {
        User findUser = userJpaRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new LoginFailedException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(user.getPassword(), findUser.getPassword())) {
            throw new LoginFailedException(ErrorCode.INVALID_PASSWORD);
        }
        String jwtToken = jwtTokenProvider.generateToken(findUser.getEmail());
        return UserDto.loginResponseDto(findUser, jwtToken);
    }

    /**
     * 로그아웃 - Redis 에 저장된 QUEUE 토큰 및 JWT 토큰 블랙리스트 등록
     * @param jwtToken
     * @param queueToken
     */
    public void logout(String jwtToken, String queueToken) {
        Claims claims = jwtTokenProvider.getClaims(jwtToken);
        long remaining = claims.getExpiration().getTime() - System.currentTimeMillis();
        userRedisRepository.expireToken(jwtToken, queueToken, remaining);
    }
}
