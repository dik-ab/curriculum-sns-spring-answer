package com.example.sns.auth;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository users;
    private final SessionTokenRepository sessions;
    private final EmailVerificationTokenRepository emailTokens;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository users, SessionTokenRepository sessions, EmailVerificationTokenRepository emailTokens) {
        this.users = users;
        this.sessions = sessions;
        this.emailTokens = emailTokens;
    }

    public User currentUser(String authorizationHeader) {
        return currentUser(authorizationHeader, null);
    }

    public User currentUser(String authorizationHeader, String sessionCookie) {
        if (sessionCookie != null && !sessionCookie.isBlank()) {
            return currentUserByToken(sessionCookie);
        }
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "ログインが必要です");
        }
        return currentUserByToken(authorizationHeader.substring("Bearer ".length()));
    }

    public User currentUserByToken(String token) {
        return sessions.findById(token)
            .map(SessionToken::getUser)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "ログインが必要です"));
    }

    @Transactional
    public User register(RegisterRequest request) {
        if (users.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "メールアドレスは既に使われています");
        }
        if (users.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ユーザー名は既に使われています");
        }
        User user = users.save(new User(
            request.email(),
            request.username(),
            request.displayName(),
            passwordEncoder.encode(request.password())
        ));
        String token = UUID.randomUUID().toString().replace("-", "");
        emailTokens.save(new EmailVerificationToken(token, user));
        System.out.println("メール確認URL: http://localhost:5173/#/verify-email?token=" + token);
        return user;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = users.findByEmail(request.email())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "メールアドレスまたはパスワードが違います"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "メールアドレスまたはパスワードが違います");
        }
        if (!user.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "メールアドレスを確認してください");
        }
        String token = UUID.randomUUID().toString();
        sessions.save(new SessionToken(token, user));
        return new LoginResponse(token);
    }

    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken record = emailTokens.findById(token)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "確認用トークンが正しくありません"));
        User user = record.getUser();
        user.verifyEmail();
        users.save(user);
        emailTokens.deleteByUser(user);
    }

    public record RegisterRequest(String email, String username, String displayName, String password) {}
    public record LoginRequest(String email, String password) {}
    public record LoginResponse(String accessToken) {}
}
