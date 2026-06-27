package com.example.sns.chat;

import com.example.sns.auth.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "conversations", uniqueConstraints = @UniqueConstraint(columnNames = {"user_one_id", "user_two_id"}))
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User userOne;

    @ManyToOne(optional = false)
    private User userTwo;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Conversation() {
    }

    public Conversation(User a, User b) {
        if (a.getId() < b.getId()) {
            this.userOne = a;
            this.userTwo = b;
        } else {
            this.userOne = b;
            this.userTwo = a;
        }
    }

    public Long getId() { return id; }
    public User getUserOne() { return userOne; }
    public User getUserTwo() { return userTwo; }

    public boolean includes(User user) {
        return userOne.getId().equals(user.getId()) || userTwo.getId().equals(user.getId());
    }

    public User partnerOf(User user) {
        return userOne.getId().equals(user.getId()) ? userTwo : userOne;
    }
}

