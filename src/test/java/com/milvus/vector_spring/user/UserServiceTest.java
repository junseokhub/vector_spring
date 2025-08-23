package com.milvus.vector_spring.user;

import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.user.dto.UserProjectsResponseDto;
import com.milvus.vector_spring.user.dto.UserSignUpRequestDto;
import com.milvus.vector_spring.user.dto.UserUpdateRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.milvus.vector_spring.common.apipayload.status.ErrorStatus.DUPLICATE_USER_EMAIL;
import static com.milvus.vector_spring.common.apipayload.status.ErrorStatus.NOT_FOUND_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserSignUpRequestDto createUserDto(String email, String username) {
        return UserSignUpRequestDto.builder()
                .email(email)
                .username(username)
                .password("Password1!")
                .build();
    }

    @Test
    void find_all_user_success() {
        userService.signUpUser(createUserDto("user1@example.com", "user1"));

        List<User> users = userService.findAllUser();

        assertThat(users).extracting("email")
                .contains("user1@example.com", "admin@admin.com");
    }

    @Test
    void find_user_by_id_success() {
        User user = userService.signUpUser(createUserDto("user2@example.com", "user2"));

        User found = userService.findOneUser(user.getId());

        assertThat(found).isEqualTo(user);
    }

    @Test
    void find_user_by_id_fail() {
        CustomException exception = assertThrows(CustomException.class, () -> {
            userService.findOneUser(999L);
        });

        assertThat(exception.getBaseCode()).isEqualTo(NOT_FOUND_USER);
    }

    @Test
    void find_user_by_email_success() {
        userService.signUpUser(createUserDto("email3@example.com", "user3"));

        User found = userService.findOneUserByEmail("email3@example.com");

        assertThat(found.getEmail()).isEqualTo("email3@example.com");
    }

    @Test
    void find_user_by_email_fail() {
        CustomException exception = assertThrows(CustomException.class, () -> {
            userService.findOneUserByEmail("none@example.com");
        });

        assertThat(exception.getBaseCode()).isEqualTo(NOT_FOUND_USER);
    }

    @Test
    void find_user_with_projects() {
        User user = userService.signUpUser(createUserDto("proj@example.com", "withproject"));

        UserProjectsResponseDto projectDto = userService.findOneUserWithProjects(user.getId());

        assertThat(projectDto).isNotNull();
        assertThat(projectDto.getId()).isEqualTo(user.getId());
    }

    @Test
    void sign_up_success() {
        UserSignUpRequestDto dto = createUserDto("new@example.com", "username");

        User savedUser = userService.signUpUser(dto);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo(dto.getEmail());
        assertThat(savedUser.getUsername()).isEqualTo(dto.getUsername());
        assertThat(passwordEncoder.matches("Password1!", savedUser.getPassword())).isTrue();
    }

    @Test
    void sign_up_duplicate_email_fail() {
        userService.signUpUser(createUserDto("duplicate@example.com", "dup"));

        CustomException exception = assertThrows(CustomException.class, () -> {
            userService.signUpUser(createUserDto("duplicate@example.com", "dup"));
        });

        assertThat(exception.getBaseCode()).isEqualTo(DUPLICATE_USER_EMAIL);
    }

    @Test
    void update_user_success_same_email() {
        User user = userService.signUpUser(createUserDto("user@example.com", "before"));

        UserUpdateRequestDto updateDto = UserUpdateRequestDto.builder()
                .email("user@example.com")
                .username("after")
                .build();

        User updated = userService.updateUser(user.getId(), updateDto);

        assertThat(updated.getUsername()).isEqualTo("after");
    }

    @Test
    void update_user_fail_user_not_found() {
        UserUpdateRequestDto updateDto = UserUpdateRequestDto.builder()
                .email("notfound@example.com")
                .build();

        CustomException exception = assertThrows(CustomException.class, () -> {
            userService.updateUser(999L, updateDto);
        });

        assertThat(exception.getBaseCode()).isEqualTo(NOT_FOUND_USER);
    }

    @Test
    void update_user_fail_duplicate_email() {
        User user1 = userService.signUpUser(createUserDto("existing@example.com", "user1"));
        userService.signUpUser(createUserDto("new@example.com", "user2"));

        UserUpdateRequestDto updateDto = UserUpdateRequestDto.builder()
                .email("new@example.com")
                .build();

        CustomException exception = assertThrows(CustomException.class, () -> {
            userService.updateUser(user1.getId(), updateDto);
        });

        assertThat(exception.getBaseCode()).isEqualTo(DUPLICATE_USER_EMAIL);
    }
}

