package com.example.sns.users;

import com.example.sns.auth.AuthService;
import com.example.sns.auth.User;
import com.example.sns.auth.UserRepository;
import com.example.sns.posts.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/users")
public class UsersController {
    private final AuthService auth;
    private final UserRepository users;
    private final FollowRepository follows;
    private final PostRepository posts;
    private final LikeRepository likes;

    public UsersController(AuthService auth, UserRepository users, FollowRepository follows, PostRepository posts, LikeRepository likes) {
        this.auth = auth;
        this.users = users;
        this.follows = follows;
        this.posts = posts;
        this.likes = likes;
    }

    @GetMapping("/{username}")
    public UserProfileDto profile(@RequestHeader(value = "Authorization", required = false) String authorization, @PathVariable String username) {
        User viewer = auth.currentUser(authorization);
        User user = findByUsername(username);
        return UserProfileDto.from(user, follows.countByFollowee(user), follows.countByFollower(user), follows.existsByFollowerAndFollowee(viewer, user));
    }

    @GetMapping("/{username}/posts")
    public List<PostDto> userPosts(@RequestHeader(value = "Authorization", required = false) String authorization, @PathVariable String username) {
        User viewer = auth.currentUser(authorization);
        User user = findByUsername(username);
        return posts.findByAuthorOrderByCreatedAtDesc(user).stream().map(post -> PostDto.from(post, viewer, likes)).toList();
    }

    @PostMapping("/{username}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void follow(@RequestHeader(value = "Authorization", required = false) String authorization, @PathVariable String username) {
        User viewer = auth.currentUser(authorization);
        User followee = findByUsername(username);
        if (viewer.getId().equals(followee.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "自分はフォローできません");
        }
        if (!follows.existsByFollowerAndFollowee(viewer, followee)) {
            follows.save(new Follow(viewer, followee));
        }
    }

    @DeleteMapping("/{username}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollow(@RequestHeader(value = "Authorization", required = false) String authorization, @PathVariable String username) {
        User viewer = auth.currentUser(authorization);
        User followee = findByUsername(username);
        follows.findByFollowerAndFollowee(viewer, followee).ifPresent(follows::delete);
    }

    @PatchMapping("/me")
    public UserDto updateMe(@RequestHeader(value = "Authorization", required = false) String authorization, @RequestBody UpdateProfileRequest request) {
        User viewer = auth.currentUser(authorization);
        viewer.updateProfile(request.displayName(), request.bio(), request.avatarUrl());
        return UserDto.from(users.save(viewer));
    }

    private User findByUsername(String username) {
        return users.findByUsername(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーがありません"));
    }

    public record UpdateProfileRequest(String displayName, String bio, String avatarUrl) {}
    public record UserProfileDto(Long id, String username, String displayName, String bio, String avatarUrl, long followersCount, long followingCount, boolean isFollowing) {
        static UserProfileDto from(User user, long followersCount, long followingCount, boolean isFollowing) {
            return new UserProfileDto(user.getId(), user.getUsername(), user.getDisplayName(), user.getBio(), user.getAvatarUrl(), followersCount, followingCount, isFollowing);
        }
    }
}

