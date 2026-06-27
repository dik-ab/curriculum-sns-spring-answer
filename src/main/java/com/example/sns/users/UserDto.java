package com.example.sns.users;

import com.example.sns.auth.User;

public record UserDto(Long id, String username, String displayName, String bio, String avatarUrl) {
    public static UserDto from(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getDisplayName(), user.getBio(), user.getAvatarUrl());
    }
}

