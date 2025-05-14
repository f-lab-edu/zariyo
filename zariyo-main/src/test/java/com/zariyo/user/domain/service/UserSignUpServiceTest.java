package com.zariyo.user.domain.service;

import com.zariyo.user.api.dto.UserDto;
import com.zariyo.user.domain.entity.User;
import com.zariyo.user.infra.UserJpaRepository;
import com.zariyo.common.exception.custom.SingupException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSignUpServiceTest {

    @Mock
    private UserJpaRepository userJpaRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserSignUpService userSignUpService;

    @Test
    @DisplayName("이메일이 존재하면 true를 반환")
    void returnsTrueWhenEmailExists() {
        // given
        String email = "test@example.com";
        when(userJpaRepository.existsByEmail(email)).thenReturn(true);
        // when
        boolean result = userSignUpService.checkEmail(email);
        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("이메일이 존재하지 않으면 false를 반환")
    void returnsFalseWhenEmailDoesNotExist() {
        // given
        String email = "test@example.com";
        when(userJpaRepository.existsByEmail(email)).thenReturn(false);
        // when
        boolean result = userSignUpService.checkEmail(email);
        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("닉네임이 존재하면 true를 반환")
    void returnsTrueWhenNicknameExists() {
        // given
        String nickname = "test";
        when(userJpaRepository.existsByNickName(nickname)).thenReturn(true);
        // when
        boolean result = userSignUpService.checkNickName(nickname);
        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("닉네임이 존재하지 않으면 false를 반환")
    void returnsFalseWhenNicknameDoesNotExist() {
        // given
        String nickname = "test";
        when(userJpaRepository.existsByNickName(nickname)).thenReturn(false);
        // when
        boolean result = userSignUpService.checkNickName(nickname);
        // then
        assertThat(result).isFalse();
    }


    @Test
    @DisplayName("회원 저장 중 예외가 발생하면 SingupException을 throw")
    void throwsExceptionWhenSaveFails() {
        // given
        User user = UserDto.toEntity(new UserDto(1L, "token-token-token","test@example.com", "test", "nickname"));

        // when & then
        when(userJpaRepository.save(user)).thenThrow(new RuntimeException("DB Error"));
        assertThatThrownBy(() -> userSignUpService.signUp(user))
                .isInstanceOf(SingupException.class);
    }
}
