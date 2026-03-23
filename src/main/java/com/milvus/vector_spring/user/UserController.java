package com.milvus.vector_spring.user;

import com.milvus.vector_spring.common.exception.CustomException;
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
        List<User> users = userService.findAllUser();
        return users.stream()
                .map(UserResponseDto::from)
                .toList();
    }

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto signUpUser(@Validated @RequestBody UserSignUpRequestDto userSignUpRequestDto) throws CustomException {
        User user = userService.signUpUser(userSignUpRequestDto);
        return UserResponseDto.from(user);
    }

    @PostMapping("/update/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto updateUser(@PathVariable() Long id, @Validated @RequestBody UserUpdateRequestDto userUpdateRequestDto) throws CustomException {
        User user = userService.updateUser(id, userUpdateRequestDto);
        return UserResponseDto.from(user);
    }
}
