package com.milvus.vector_spring.config.jwt;

import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.user.User;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final static String HEADER_AUTHORIZATION = "Authorization";

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);
        String token = getAccessToken(authorizationHeader);

        if (token != null) {
            if (jwtTokenProvider.validateToken(token)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                handleExpiredAccessToken(request, response);
            }
        }
        filterChain.doFilter(request, response);
    }

    private void handleExpiredAccessToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            String token = getAccessToken(request.getHeader(HEADER_AUTHORIZATION));
            Claims claims = jwtTokenProvider.expiredTokenGetPayload(token);
            User user = User.builder()
                    .id(claims.get("userId", Long.class))
                    .email(claims.get("email", String.class))
                    .role(claims.get("role", String.class))
                    .build();

            if (!jwtTokenProvider.validateRefreshToken(user)) {
                throw new CustomException(ErrorStatus.EXPIRED_REFRESH_TOKEN);
            }

            String newAccessToken = jwtTokenProvider.generateAccessToken(user);

            Authentication authentication = jwtTokenProvider.getAuthentication(newAccessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            response.setHeader("New-Access-Token", newAccessToken);
            request.setAttribute("New-Access-Token", newAccessToken);
        } catch (CustomException e) {
            request.setAttribute("exception", e.getBaseCode());
        } catch (Exception e) {
            request.setAttribute("exception", ErrorStatus.INVALID_TOKEN);
        }
    }

    private String getAccessToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer")) {
            return authorizationHeader.replace("Bearer", "").trim();
        }
        return null;
    }
}
