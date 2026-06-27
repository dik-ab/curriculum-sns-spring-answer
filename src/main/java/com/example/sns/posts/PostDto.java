package com.example.sns.posts;

import com.example.sns.auth.User;
import com.example.sns.users.UserDto;
import java.time.Instant;

public record PostDto(Long id, String content, Instant createdAt, UserDto author, long likeCount, boolean likedByMe) {
    public static PostDto from(Post post, User viewer, LikeRepository likes) {
        return new PostDto(
            post.getId(),
            post.getContent(),
            post.getCreatedAt(),
            UserDto.from(post.getAuthor()),
            likes.countByPost(post),
            likes.existsByUserAndPost(viewer, post)
        );
    }
}

