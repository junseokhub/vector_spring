package com.milvus.vector_spring.config.jwt;

import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
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
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractBearerToken(request);

        if (token != null) {
            if (jwtTokenProvider.validateToken(token)) {
                setAuthentication(token);
            } else {
                // 만료 시 401만 반환 — 재발급은 클라이언트가 /auth/reissue로
                request.setAttribute("exception", ErrorStatus.INVALID_ACCESS_TOKEN);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String token) {
        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER_AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}