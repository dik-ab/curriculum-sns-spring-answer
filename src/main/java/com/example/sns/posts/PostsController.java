package com.example.sns.posts;

import com.example.sns.auth.AuthService;
import com.example.sns.auth.User;
import com.example.sns.users.FollowRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/posts")
public class PostsController {
    private final AuthService auth;
    private final PostRepository posts;
    private final LikeRepository likes;
    private final FollowRepository follows;

    public PostsController(AuthService auth, PostRepository posts, LikeRepository likes, FollowRepository follows) {
        this.auth = auth;
        this.posts = posts;
        this.likes = likes;
        this.follows = follows;
    }

    @GetMapping
    public List<PostDto> index(@RequestHeader(value = "Authorization", required = false) String authorization) {
        User viewer = auth.currentUser(authorization);
        return posts.findAllByOrderByCreatedAtDesc().stream().map(post -> PostDto.from(post, viewer, likes)).toList();
    }

    @GetMapping("/timeline")
    public List<PostDto> following(@RequestHeader(value = "Authorization", required = false) String authorization) {
        User viewer = auth.currentUser(authorization);
        List<User> authors = new ArrayList<>();
        authors.add(viewer);
        follows.findByFollower(viewer).forEach(follow -> authors.add(follow.getFollowee()));
        return posts.findByAuthorInOrderByCreatedAtDesc(authors).stream().map(post -> PostDto.from(post, viewer, likes)).toList();
    }

    @PostMapping
    public PostDto create(@RequestHeader(value = "Authorization", required = false) String authorization, @RequestBody CreatePostRequest request) {
        User viewer = auth.currentUser(authorization);
        if (request.content() == null || request.content().isBlank() || request.content().length() > 280) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "投稿は1文字以上280文字以内です");
        }
        return PostDto.from(posts.save(new Post(viewer, request.content().trim())), viewer, likes);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader(value = "Authorization", required = false) String authorization, @PathVariable Long id) {
        User viewer = auth.currentUser(authorization);
        Post post = posts.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "投稿がありません"));
        if (!post.getAuthor().getId().equals(viewer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "他人の投稿は削除できません");
        }
        posts.delete(post);
    }

    @PostMapping("/{id}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void like(@RequestHeader(value = "Authorization", required = false) String authorization, @PathVariable Long id) {
        User viewer = auth.currentUser(authorization);
        Post post = posts.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "投稿がありません"));
        if (!likes.existsByUserAndPost(viewer, post)) {
            likes.save(new Like(viewer, post));
        }
    }

    @DeleteMapping("/{id}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlike(@RequestHeader(value = "Authorization", required = false) String authorization, @PathVariable Long id) {
        User viewer = auth.currentUser(authorization);
        Post post = posts.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "投稿がありません"));
        likes.findByUserAndPost(viewer, post).ifPresent(likes::delete);
    }

    public record CreatePostRequest(String content) {}
}

