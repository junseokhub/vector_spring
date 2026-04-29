package com.milvus.vector_spring.user;

import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> findAllUser() {
        return userRepository.findAll();
    }

    public User findOneUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorStatus.NOT_FOUND_USER));
    }

    @Transactional
    public User updateLoginAt(User user) {
        user.updateLoginAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User findOneUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorStatus.NOT_FOUND_USER));
    }

    private void duplicateEmailCheck(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            throw new CustomException(ErrorStatus.DUPLICATE_USER_EMAIL);
        });
    }

    @Transactional
    public User signUp(String email, String username, String password) {
        duplicateEmailCheck(email);
        User user = User.builder()
                .email(email)
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(Role.USER.getValue())
                .build();
        return userRepository.save(user);
    }

    @Transactional
    public User update(Long id, String email, String username) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorStatus.NOT_FOUND_USER));
        if (!user.getEmail().equals(email)) {
            duplicateEmailCheck(email);
        }
        user.update(email, username, user.getPassword());
        return user;
    }
}
