package com.milvus.vector_spring.user;

import com.milvus.vector_spring.user.dto.UserResponseDto;
import com.milvus.vector_spring.user.dto.UserSignUpRequestDto;
import com.milvus.vector_spring.user.dto.UserUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
        return userService.findAllUser().stream()
                .map(UserResponseDto::from)
                .toList();
    }

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto signUpUser(@Validated @RequestBody UserSignUpRequestDto request) {
        return UserResponseDto.from(userService.signUp(request.email(), request.username(), request.password()));
    }

    @PatchMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto updateUser(@PathVariable Long id, @Validated @RequestBody UserUpdateRequestDto request) {
        return UserResponseDto.from(userService.update(id, request.email(), request.username()));
    }
}
