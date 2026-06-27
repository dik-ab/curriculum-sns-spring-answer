package com.example.sns.users;

import com.example.sns.auth.User;
import jakarta.persistence.*;

@Entity
@Table(name = "follows", uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "followee_id"}))
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User follower;

    @ManyToOne(optional = false)
    private User followee;

    protected Follow() {
    }

    public Follow(User follower, User followee) {
        this.follower = follower;
        this.followee = followee;
    }

    public User getFollower() { return follower; }
    public User getFollowee() { return followee; }
}

