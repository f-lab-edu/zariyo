package com.zariyo.user.application;

import com.zariyo.common.exception.ErrorCode;
import com.zariyo.common.exception.custom.LogoutFailedException;
import com.zariyo.user.api.dto.UserDto;
import com.zariyo.user.domain.service.UserAuthService;
import com.zariyo.user.domain.service.UserSignUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * UserAppService 는 api 와 Domain - service 계층을 연결하는 역할을 합니다.
 */
@Component
@RequiredArgsConstructor
public class UserAppService {

    private final UserSignUpService userSignUpService;
    private final UserAuthService userAuthService;

    public boolean checkEmail(String email) {
        return userSignUpService.checkEmail(email);
    }

    public boolean checkNickName(String nickname) {
        return userSignUpService.checkNickName(nickname);
    }

    public void signUp(UserDto userDto) {
        userSignUpService.signUp(UserDto.toEntity(userDto));
    }

    public UserDto login(UserDto userDto) {
        return userAuthService.login(UserDto.toEntity(userDto));
    }

    public void logout(String jwtToken, String queueToken) {
        if(jwtToken != null && jwtToken.startsWith("Bearer ") && queueToken != null) {
            jwtToken = jwtToken.substring(7);
            userAuthService.logout(jwtToken, queueToken);
        } else {
            throw new LogoutFailedException(ErrorCode.LOGOUT_FAILED);
        }
    }
}
