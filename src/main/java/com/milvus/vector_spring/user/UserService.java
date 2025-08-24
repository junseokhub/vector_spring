package com.milvus.vector_spring.user;

import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.content.ContentRepository;
import com.milvus.vector_spring.invite.InviteRepository;
import com.milvus.vector_spring.project.Project;
import com.milvus.vector_spring.project.ProjectRepository;
import com.milvus.vector_spring.user.dto.UserProjectsResponseDto;
import com.milvus.vector_spring.user.dto.UserSignUpRequestDto;
import com.milvus.vector_spring.user.dto.UserUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final InviteRepository inviteRepository;
    private final ProjectRepository projectRepository;
    private final ContentRepository contentRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> findAllUser() {
        return userRepository.findAll();
    }

    public User findOneUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorStatus.NOT_FOUND_USER));
    }

    public UserProjectsResponseDto findOneUserWithProjects(Long id) {
        User user = userRepository.findOneUserWithProjects(id);
        List<Project> projects = projectRepository.findAllByCreatedBy(user);
        return UserProjectsResponseDto.of(user, projects);
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
    public User signUpUser(UserSignUpRequestDto userSignUpRequestDto) {
        duplicateEmailCheck(userSignUpRequestDto.getEmail());
        User user = User.builder()
                .email(userSignUpRequestDto.getEmail())
                .username(userSignUpRequestDto.getUsername())
                .password(passwordEncoder.encode(userSignUpRequestDto.getPassword()))
                .role("ROLE_USER")
                .build();
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, UserUpdateRequestDto userUpdateRequestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorStatus.NOT_FOUND_USER));

        if (!user.getEmail().equals(userUpdateRequestDto.getEmail())) {
            duplicateEmailCheck(userUpdateRequestDto.getEmail());
        }

        User updatedUser = User.builder()
                .id(user.getId())
                .email(userUpdateRequestDto.getEmail())
                .username(userUpdateRequestDto.getUsername())
                .password(user.getPassword())
                .build();
        return userRepository.save(updatedUser);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorStatus.NOT_FOUND_USER));

        inviteRepository.deleteByUserId(userId);
        projectRepository.deleteByUserId(userId);
        contentRepository.deleteByUserId(userId);
        userRepository.delete(user);
    }
}