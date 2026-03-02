package com.milvus.vector_spring.config;

import com.milvus.vector_spring.util.properties.CommonProperties;
import com.milvus.vector_spring.user.User;
import com.milvus.vector_spring.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final CommonProperties commonProperties;

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            Optional<User> existEmail = userRepository.findByEmail(commonProperties.adminEmail());
            if (existEmail.isEmpty()) {
                User admin = User.builder()
                        .id(1L)
                        .email(commonProperties.adminEmail())
                        .username("ADMIN")
                        .password(passwordEncoder.encode(commonProperties.adminPassword()))
                        .role("ROLE_ADMIN")
                        .build();
                userRepository.save(admin);
                System.out.println("✅ Admin account created successfully.");
            } else {
                System.out.println("ℹ️ Admin account already exists.");
            }
        };
    }
}