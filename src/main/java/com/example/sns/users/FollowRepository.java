package com.example.sns.users;

import com.example.sns.auth.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerAndFollowee(User follower, User followee);
    Optional<Follow> findByFollowerAndFollowee(User follower, User followee);
    long countByFollower(User follower);
    long countByFollowee(User followee);
    List<Follow> findByFollower(User follower);
}

