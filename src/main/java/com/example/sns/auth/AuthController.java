package com.example.sns.auth;

import com.example.sns.users.UserDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    public UserDto register(@RequestBody AuthService.RegisterRequest request) {
        return UserDto.from(auth.register(request));
    }

    @PostMapping("/login")
    public AuthService.LoginResponse login(@RequestBody AuthService.LoginRequest request) {
        return auth.login(request);
    }

    @GetMapping("/verify-email")
    public MessageResponse verifyEmail(@RequestParam String token) {
        auth.verifyEmail(token);
        return new MessageResponse("メールアドレスを確認しました");
    }

    @GetMapping("/me")
    public UserDto me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return UserDto.from(auth.currentUser(authorization));
    }

    public record MessageResponse(String message) {}
}

