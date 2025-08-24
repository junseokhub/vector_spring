package com.milvus.vector_spring.auth;

import com.milvus.vector_spring.auth.dto.UserLoginRequestDto;
import com.milvus.vector_spring.auth.dto.UserLoginResponseDto;
import com.milvus.vector_spring.common.annotation.NoAuthRequired;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/auth")
@RequiredArgsConstructor
@RestController
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @NoAuthRequired()
    public ResponseEntity<UserLoginResponseDto> login(@RequestBody UserLoginRequestDto userLoginRequestDto) {
        UserLoginResponseDto userLoginResponseDto = authService.login(userLoginRequestDto);
        return ResponseEntity.ok(userLoginResponseDto);
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout() {
        authService.logout();
        return ResponseEntity.ok("로그아웃 완료!");
    }

    @GetMapping("/check")
    public ResponseEntity<UserLoginResponseDto> check() {
        UserLoginResponseDto userCheck = authService.check();
        return ResponseEntity.ok(userCheck);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String test() {
        return "넌 어드민";
    }
}