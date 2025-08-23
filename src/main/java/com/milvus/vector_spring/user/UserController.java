package com.milvus.vector_spring.user;

import com.milvus.vector_spring.common.annotation.NoAuthRequired;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.user.dto.UserProjectsResponseDto;
import com.milvus.vector_spring.user.dto.UserResponseDto;
import com.milvus.vector_spring.user.dto.UserSignUpRequestDto;
import com.milvus.vector_spring.user.dto.UserUpdateRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;


    @GetMapping()
    public List<UserResponseDto> findAllUser() {
        List<User> users = userService.findAllUser();
        return users.stream()
                .map(UserResponseDto::userResponseDto)
                .toList();
    }

    @PostMapping("/sign-up")
    @NoAuthRequired
    public ResponseEntity<UserResponseDto> signUpUser(@Validated @RequestBody @Valid UserSignUpRequestDto userSignUpRequestDto) throws CustomException {
        User user = userService.signUpUser(userSignUpRequestDto);
        return ResponseEntity.ok(UserResponseDto.userResponseDto(user));
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable() Long id, @Validated @RequestBody UserUpdateRequestDto userUpdateRequestDto) throws CustomException {
        User user = userService.updateUser(id, userUpdateRequestDto);
        return ResponseEntity.ok(UserResponseDto.userResponseDto(user));
    }

    @GetMapping("/project/{id}")
    public UserProjectsResponseDto getUser(@PathVariable("id") Long id) throws CustomException {
        return userService.findOneUserWithProjects(id);
    }
}
