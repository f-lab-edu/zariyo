package com.zariyo.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zariyo.config.TestContainerConfig;
import com.zariyo.user.api.dto.UserDto;
import com.zariyo.user.domain.entity.User;
import com.zariyo.user.infra.UserJpaRepository;
import com.zariyo.user.security.validation.TokenValidation;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc // MockMvc 자동 설정 - Http 요청을 테스트하기 위한 MockMvc 객체를 자동으로 설정
class UserIntegrationTest extends TestContainerConfig {
    @Autowired private MockMvc mockMvc;
    @Autowired private StringRedisTemplate mainRedisTemplate;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserJpaRepository userRepository;
    @Autowired private TokenValidation tokenValidation;

    private final String queueToken = "test-queue-token";

    @BeforeEach
    void setUp() {
        // 테스트용 토큰 설정
        mainRedisTemplate.opsForValue().set("main:" + queueToken, String.valueOf(System.currentTimeMillis()));
    }

    @Test
    @DisplayName("대기열 토큰이 없으면 401 반환")
    void whenNoQueueToken_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/user/check/email").param("email", "test@example.com"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("QUEUE_TOKEN_REQUIRED"));
    }

    @Test
    @DisplayName("회원가입 전체 플로우 테스트")
    void signupFlowTest() throws Exception {
        String email = "signup@example.com";
        String password = "password123";
        String nickname = "signupTester";
        
        mockMvc.perform(get("/user/check/email")
                        .param("email", email)
                        .header("X-QUEUE-TOKEN", queueToken))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        mockMvc.perform(get("/user/check/nickname")
                        .param("nickname", nickname)
                        .header("X-QUEUE-TOKEN", queueToken))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        UserDto request = UserDto.builder()
                .email(email)
                .password(password)
                .nickName(nickname)
                .build();

        mockMvc.perform(post("/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-QUEUE-TOKEN", queueToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        User savedUser = userRepository.findByEmail(email).orElseThrow();
        assertThat(savedUser.getNickName()).isEqualTo(nickname);
    }

    @Test
    @DisplayName("로그인 및 로그아웃 플로우 테스트")
    void loginLogoutFlowTest() throws Exception {
        String email = "login@example.com";
        String password = "password123";
        String nickname = "loginTester";
        
        // 먼저 회원가입
        UserDto signupRequest = UserDto.builder()
                .email(email)
                .password(password)
                .nickName(nickname)
                .build();

        mockMvc.perform(post("/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-QUEUE-TOKEN", queueToken)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // 로그인
        UserDto loginRequest = UserDto.builder()
                .email(email)
                .password(password)
                .build();

        MvcResult result = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-QUEUE-TOKEN", queueToken)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String jwtToken = objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class).getToken();
        assertNotNull(jwtToken);

        // 로그아웃
        mockMvc.perform(post("/user/logout")
                        .header("Authorization", "Bearer " + jwtToken)
                        .header("X-QUEUE-TOKEN", queueToken))
                .andExpect(status().isOk())
                .andExpect(content().string("로그아웃 완료"));

        assertTrue(tokenValidation.isBlacklisted(jwtToken));
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 401 반환")
    void loginFailsWithWrongPassword() throws Exception {
        String email = "wrong@example.com";
        String password = "correctPassword";
        String nickname = "wrongTester";
        
        // 먼저 회원가입
        UserDto signupRequest = UserDto.builder()
                .email(email)
                .password(password)
                .nickName(nickname)
                .build();

        mockMvc.perform(post("/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-QUEUE-TOKEN", queueToken)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // 잘못된 비밀번호로 로그인 시도
        UserDto loginRequest = UserDto.builder()
                .email(email)
                .password("wrongPassword")
                .build();

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-QUEUE-TOKEN", queueToken)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_PASSWORD"));
    }
}
