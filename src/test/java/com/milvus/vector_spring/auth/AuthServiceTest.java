package com.milvus.vector_spring.auth;

import com.milvus.vector_spring.auth.dto.UserLoginRequestDto;
import com.milvus.vector_spring.auth.dto.UserLoginResponseDto;
import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.config.jwt.JwtTokenProvider;
import com.milvus.vector_spring.user.User;
import com.milvus.vector_spring.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthServiceTest {

    private static final String TEST_EMAIL = "login@example.com";
    private static final String TEST_USERNAME = "login";
    private static final String TEST_PASSWORD = "password";

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private User user;

    private HttpServletRequest request;

    @BeforeAll
    void setUp() {
        user = userRepository.findByEmail(TEST_EMAIL)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(TEST_EMAIL)
                            .username(TEST_USERNAME)
                            .password(passwordEncoder.encode(TEST_PASSWORD))
                            .build();
                    return userRepository.save(newUser);
                });
    }

    @BeforeEach
    void setupMocks() {
        request = Mockito.mock(HttpServletRequest.class);
        injectRequest(authService, request);
    }

    @Test
    void login_success() {
        UserLoginRequestDto dto = UserLoginRequestDto.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();

        UserLoginResponseDto response = authService.login(dto);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(response.getAccessToken()).isNotEmpty();
    }

    @Test
    void login_fail_wrong_password() {
        UserLoginRequestDto dto = UserLoginRequestDto.builder()
                .email(TEST_EMAIL)
                .password("wrongpassword")
                .build();

        CustomException exception = assertThrows(CustomException.class, () -> authService.login(dto));
        assertThat(exception.getBaseCode()).isEqualTo(ErrorStatus.NOT_PASSWORD_MATCHES);
    }

    @Test
    void check_valid_token() {
        String accessToken = jwtTokenProvider.generateAccessToken(user);

        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer " + accessToken);

        injectRequest(authService, request);

        UserLoginResponseDto response = authService.check();

        assertThat(response.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
    }

    private void injectRequest(AuthService authService, HttpServletRequest request) {
        try {
            var field = AuthService.class.getDeclaredField("request");
            field.setAccessible(true);
            field.set(authService, request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
