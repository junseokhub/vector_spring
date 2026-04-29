package com.milvus.vector_spring.config.jwt;

import com.milvus.vector_spring.user.User;
import com.milvus.vector_spring.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.token().secretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = now.plusSeconds(jwtProperties.token().accessExpiration() / 1000);
        Header jwtHeader = Jwts.header().type("JWT").build();

        return Jwts.builder()
                .header().add(jwtHeader).and()
                .subject(user.getEmail())
                .claims(userToMap(user))
                .issuedAt(java.sql.Timestamp.valueOf(now))
                .expiration(java.sql.Timestamp.valueOf(expiryDate))
                .signWith(this.getSigningKey())
                .compact();
    }

    public String generateRefreshToken(String email) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = now.plusSeconds(jwtProperties.token().refreshExpiration() / 1000);

        return Jwts.builder()
                .claim("email", email)
                .issuedAt(java.sql.Timestamp.valueOf(now))
                .expiration(java.sql.Timestamp.valueOf(expiryDate))
                .signWith(this.getSigningKey())
                .compact();
    }

    public long getRefreshTokenTtlSeconds() {
        return jwtProperties.token().refreshExpiration() / 1000;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(this.getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);

        User principal = User.builder()
                .id(claims.get("userId", Long.class))
                .username(claims.get("userName", String.class))
                .email(claims.get("email", String.class))
                .role(claims.get("role", String.class))
                .build();

        return new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(this.getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getRemainingExpiryMillis(String token) {
        try {
            Date expiry = getClaims(token).getExpiration();
            return expiry.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            return 0;
        }
    }

    private Map<String, Object> userToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", user.getId());
        map.put("email", user.getEmail());
        map.put("userName", user.getUsername());
        map.put("role", user.getRole());
        return map;
    }
}