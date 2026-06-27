package com.example.sns.auth;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "session_tokens")
public class SessionToken {
    @Id
    private String token;

    @ManyToOne(optional = false)
    private User user;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected SessionToken() {
    }

    public SessionToken(String token, User user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public User getUser() { return user; }
    public Instant getCreatedAt() { return createdAt; }
}

