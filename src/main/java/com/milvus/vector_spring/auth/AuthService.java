package com.milvus.vector_spring.auth;

import com.milvus.vector_spring.auth.dto.UserLoginRequestDto;
import com.milvus.vector_spring.auth.dto.UserLoginResponseDto;
import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.common.service.RedisService;
import com.milvus.vector_spring.config.jwt.JwtTokenProvider;
import com.milvus.vector_spring.user.User;
import com.milvus.vector_spring.user.UserDetailServiceImpl;
import com.milvus.vector_spring.user.UserRepository;
import com.milvus.vector_spring.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserDetailServiceImpl userDetailServiceImpl;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RedisService redisService;
    private final UserService userService;
    private final HttpServletRequest request;

    public UserLoginResponseDto login(UserLoginRequestDto userLoginRequestDto) {
        User user = userService.findOneUserByEmail(userLoginRequestDto.getEmail());
        if (!bCryptPasswordEncoder.matches(userLoginRequestDto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorStatus.NOT_PASSWORD_MATCHES);
        };
        user.updateLoginAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        jwtTokenProvider.generateRefreshToken(user);
        return new UserLoginResponseDto(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getUsername(),
                savedUser.getRole(),
                accessToken,
                savedUser.getLoginAt()
        );
    }

    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException(ErrorStatus.INVALID_ACCESS_TOKEN);
        }
        User user = (User) authentication.getPrincipal();
        String redisKey = "refreshToken:" + user.getEmail();
        redisService.deleteRedis(redisKey);
    }

    public UserLoginResponseDto check() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException(ErrorStatus.INVALID_ACCESS_TOKEN);
        }

        User user = (User) authentication.getPrincipal();
        String currentToken = getToken();

        return new UserLoginResponseDto(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole(),
                currentToken,
                user.getLoginAt()
        );
    }

    private String getToken() {
        String newToken = (String) request.getAttribute("New-Access-Token");
        if (newToken != null) {
            return newToken;
        }
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7).trim();
        }
        throw new CustomException(ErrorStatus.INVALID_ACCESS_TOKEN);
    }
}
