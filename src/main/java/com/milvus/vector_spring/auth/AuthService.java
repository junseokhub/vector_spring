package com.milvus.vector_spring.auth;

import com.milvus.vector_spring.auth.dto.AuthTokenDto;
import com.milvus.vector_spring.auth.dto.UserLoginResponseDto;
import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.config.jwt.JwtTokenProvider;
import com.milvus.vector_spring.config.jwt.TokenBlacklistService;
import com.milvus.vector_spring.user.User;
import com.milvus.vector_spring.user.UserService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String REFRESH_TOKEN_PREFIX = "refreshToken:";

    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserService userService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthTokenDto login(String email, String password) {
        User user = userService.findOneUserByEmail(email);

        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new CustomException(ErrorStatus.NOT_PASSWORD_MATCHES);
        }

        user.updateLoginAt(LocalDateTime.now());
        User savedUser = userService.updateLoginAt(user);

        String accessToken = jwtTokenProvider.generateAccessToken(savedUser);
        String refreshToken = saveRefreshToken(savedUser.getEmail());

        return new AuthTokenDto(
                new UserLoginResponseDto(
                        savedUser.getId(),
                        savedUser.getEmail(),
                        savedUser.getUsername(),
                        savedUser.getRole(),
                        accessToken,
                        savedUser.getLoginAt()
                ),
                refreshToken
        );
    }

    public AuthTokenDto reissue(String refreshToken) {
        if (refreshToken == null) {
            throw new CustomException(ErrorStatus.EXPIRED_REFRESH_TOKEN);
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorStatus.EXPIRED_REFRESH_TOKEN);
        }

        Claims claims = jwtTokenProvider.getClaims(refreshToken);
        String email = claims.get("email", String.class);

        String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + email);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + email);
            throw new CustomException(ErrorStatus.EXPIRED_REFRESH_TOKEN);
        }

        User user = userService.findOneUserByEmail(email);
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = saveRefreshToken(email);

        return new AuthTokenDto(
                new UserLoginResponseDto(
                        user.getId(),
                        user.getEmail(),
                        user.getUsername(),
                        user.getRole(),
                        newAccessToken,
                        user.getLoginAt()
                ),
                newRefreshToken
        );
    }

    public User logout(String accessToken) {
        User user = getAuthenticatedUser();
        long remainingMillis = jwtTokenProvider.getRemainingExpiryMillis(accessToken);
        tokenBlacklistService.add(accessToken, remainingMillis);
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + user.getEmail());
        return user;
    }

    public UserLoginResponseDto check(String accessToken) {
        User user = getAuthenticatedUser();
        return new UserLoginResponseDto(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole(),
                accessToken,
                user.getLoginAt()
        );
    }

    private String saveRefreshToken(String email) {
        String refreshToken = jwtTokenProvider.generateRefreshToken(email);
        try {
            redisTemplate.opsForValue().set(
                    REFRESH_TOKEN_PREFIX + email,
                    refreshToken,
                    jwtTokenProvider.getRefreshTokenTtlSeconds(),
                    TimeUnit.SECONDS
            );
        } catch (Exception e) {
            log.error("Redis refreshToken 저장 실패: {}", e.getMessage());
            throw new CustomException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
        return refreshToken;
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException(ErrorStatus.INVALID_ACCESS_TOKEN);
        }
        return (User) authentication.getPrincipal();
    }
}
