package com.milvus.vector_spring.util.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mail")
public record MailProperties (
        AdminMail adminMail,
        Smtp smtp
) {
    public record Smtp(
            int port,
            Smtp.SocketFactory socketFactory
        ) {
            public record SocketFactory(
                    int port
            ) {}
    }

    public record AdminMail(String email, String password) {}
}
