package com.zariyo.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // TestMethodOrder 실행 순서 설정
class UserIntegrationTest extends TestContainerConfig {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserJpaRepository userRepository;
    @Autowired private StringRedisTemplate mainRedisTemplate;
    @Autowired private TokenValidation tokenValidation;

    private final String queueToken = "test-queue-token";
    private String jwtToken;

    private final String email = "test@example.com";
    private final String password = "password123";
    private final String nickname = "tester";

    @BeforeAll
    void setUp() {
        mainRedisTemplate.opsForValue().set("main:" + queueToken, String.valueOf(System.currentTimeMillis()));
    }

    @AfterAll
    void cleanUp() {
        userRepository.deleteAll();
        mainRedisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @Order(1)
    @DisplayName("대기열 토큰이 없으면 401 반환")
    void whenNoQueueToken_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/user/check/email").param("email", email))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("QUEUE_TOKEN_REQUIRED"));
    }

    @Test
    @Order(2)
    @DisplayName("회원가입 전체 플로우 테스트")
    void signupFlowTest() throws Exception {
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
    @Order(3)
    @DisplayName("중복 이메일 가입 시 409 반환")
    void duplicateEmailSignUpFails() throws Exception {
        UserDto duplicate = UserDto.builder()
                .email(email)
                .password("x")
                .nickName("x")
                .build();

        mockMvc.perform(post("/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-QUEUE-TOKEN", queueToken)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(4)
    @DisplayName("로그인 성공 시 JWT 반환 및 인증 요청 가능")
    void loginAndTokenTest() throws Exception {
        UserDto login = UserDto.builder()
                .email(email)
                .password(password)
                .build();

        MvcResult result = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-QUEUE-TOKEN", queueToken)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        jwtToken = objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class).getToken();
        assertNotNull(jwtToken);

        mockMvc.perform(get("/user/check/email")
                        .param("email", "another@example.com")
                        .header("Authorization", "Bearer " + jwtToken)
                        .header("X-QUEUE-TOKEN", queueToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    @DisplayName("잘못된 비밀번호로 로그인 시 401 반환")
    void loginFailsWithWrongPassword() throws Exception {
        UserDto login = UserDto.builder()
                .email(email)
                .password("wrong")
                .build();

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-QUEUE-TOKEN", queueToken)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_PASSWORD"));
    }

    @Test
    @Order(6)
    @DisplayName("로그아웃 시 토큰 블랙리스트 처리 확인")
    void logoutFlowTest() throws Exception {
        mockMvc.perform(post("/user/logout")
                        .header("Authorization", "Bearer " + jwtToken)
                        .header("X-QUEUE-TOKEN", queueToken))
                .andExpect(status().isOk())
                .andExpect(content().string("로그아웃 완료"));

        assertTrue(tokenValidation.isBlacklisted(jwtToken));
    }
}
