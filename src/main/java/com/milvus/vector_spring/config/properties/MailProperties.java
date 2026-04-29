package com.milvus.vector_spring.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mail")
public record MailProperties(
        AdminMail adminMail,
        Smtp smtp
) {
    public record AdminMail(String email, String password) {}

    public record Smtp(int port, SocketFactory socketFactory) {
        public record SocketFactory(int port) {}
    }
}
