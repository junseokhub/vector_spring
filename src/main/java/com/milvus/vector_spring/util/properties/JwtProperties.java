package com.milvus.vector_spring.util.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        Token token
) {
    public record Token(
            String secretKey,
            int accessExpiration,
            int refreshExpiration
    ) {
    }
}

//package com.milvus.vector_spring.config.properties;
//
//import lombok.Getter;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//
//@Getter
//@ConfigurationProperties(prefix = "jwt")
//public class JwtProperties {
//
//    private final Token token;
//
//    public JwtProperties(Token token) {
//        this.token = token;
//    }
//
//    @Getter
//    public static class Token {
//        private final String secretKey;
//        private final int accessExpiration;
//        private final int refreshExpiration;
//
//        public Token(String secretKey, int accessExpiration, int refreshExpiration) {
//            this.secretKey = secretKey;
//            this.accessExpiration = accessExpiration;
//            this.refreshExpiration = refreshExpiration;
//        }
//    }
//}