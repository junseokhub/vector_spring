package com.milvus.vector_spring.config;

import com.milvus.vector_spring.util.properties.CommonProperties;
import com.milvus.vector_spring.user.User;
import com.milvus.vector_spring.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final CommonProperties commonProperties;

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            Optional<User> existEmail = userRepository.findByEmail(commonProperties.adminEmail());
            if (existEmail.isEmpty()) {
                User admin = User.builder()
                        .email(commonProperties.adminEmail())
                        .username("ADMIN")
                        .password(passwordEncoder.encode(commonProperties.adminPassword()))
                        .role("ROLE_ADMIN")
                        .build();
                userRepository.save(admin);
                log.info("Admin account created successfully.");
            } else {
                log.info("Admin account already exists.");
            }
        };
    }
}