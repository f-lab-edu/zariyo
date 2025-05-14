package com.zariyo.user.domain.service;

import com.zariyo.user.domain.entity.User;
import com.zariyo.user.infra.UserJpaRepository;
import com.zariyo.common.exception.ErrorCode;
import com.zariyo.common.exception.custom.SingupException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSignUpService {

    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 - 이메일 중복 체크
     * @param email
     * @return
     */
    public boolean checkEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    /**
     * 회원가입 - 닉네임 중복 체크
     * @param nickname
     * @return
     */
    public boolean checkNickName(String nickname) {
        return userJpaRepository.existsByNickName(nickname);
    }

    /**
     * 회원가입 - 비밀번호 암호화후 저장
     * @param user
     */
    public void signUp(User user) {
        // 이메일 중복 체크
        if (userJpaRepository.existsByEmail(user.getEmail())) {
            throw new SingupException(ErrorCode.DUPLICATE_EMAIL);
        }
        try{
            String encryptedPassword = passwordEncoder.encode(user.getPassword());
            User encryptedUser = User.withEncryptedPassword(user, encryptedPassword);
            userJpaRepository.save(encryptedUser);
        }catch (Exception e){
            log.error("회원가입 실패: {}", e.getMessage());
            throw new SingupException(ErrorCode.SIGNUP_FAILED);
        }
    }
}
