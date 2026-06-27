package com.example.sns.auth;

import com.example.sns.users.UserDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<MessageResponse> login(@RequestBody AuthService.LoginRequest request) {
        AuthService.LoginResponse login = auth.login(request);
        ResponseCookie cookie = ResponseCookie.from("sns_session", login.accessToken())
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path("/")
            .maxAge(7 * 24 * 60 * 60)
            .build();
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(new MessageResponse("ログインしました"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = ResponseCookie.from("sns_session", "")
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path("/")
            .maxAge(0)
            .build();
        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .build();
    }

    @GetMapping("/verify-email")
    public MessageResponse verifyEmail(@RequestParam String token) {
        auth.verifyEmail(token);
        return new MessageResponse("メールアドレスを確認しました");
    }

    @GetMapping("/me")
    public UserDto me(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @CookieValue(value = "sns_session", required = false) String session
    ) {
        return UserDto.from(auth.currentUser(authorization, session));
    }

    public record MessageResponse(String message) {}
}
