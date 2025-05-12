package com.zariyo.user.api;

import com.zariyo.user.api.dto.UserDto;
import com.zariyo.user.application.UserAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserAppService userAppService;

    /**
     * 이메일 중복 체크
     * @param email
     * @return 중복 여부
     */
    @GetMapping("/check/email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(userAppService.checkEmail(email));
    }

    /**
     * 닉네임 중복 체크
     * @param nickname
     * @return 중복 여부
     */
    @GetMapping("/check/nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(userAppService.checkNickName(nickname));
    }

    /**
     * 회원가입
     * Exception 발생 시 advice에서 처리
     * @param userDto
     * @return 회원가입 완료
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody UserDto userDto) {
        userAppService.signUp(userDto);
        return ResponseEntity.ok("회원가입 완료");
    }

    /**
     * 로그인
     * @param userDto
     * @return 로그인 성공 시 JWT 토큰 반환
     */
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody UserDto userDto) {
        return ResponseEntity.ok(userAppService.login(userDto));
    }

    /**
     * 로그아웃
     * @param jwtToken
     * @param queueToken
     * @return 로그아웃 완료
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String jwtToken,
                                         @RequestHeader("X-QUEUE-TOKEN") String queueToken) {
        userAppService.logout(jwtToken, queueToken);
        return ResponseEntity.ok("로그아웃 완료");
    }
}
