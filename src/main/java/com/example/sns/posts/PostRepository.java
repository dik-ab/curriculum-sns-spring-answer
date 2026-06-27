package com.example.sns.posts;

import com.example.sns.auth.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();
    List<Post> findByAuthorOrderByCreatedAtDesc(User author);
    List<Post> findByAuthorInOrderByCreatedAtDesc(List<User> authors);
}

