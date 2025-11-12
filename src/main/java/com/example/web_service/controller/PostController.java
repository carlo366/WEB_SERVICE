package com.example.web_service.controller;

import com.example.web_service.dto.Response;
import com.example.web_service.entity.Post;
import com.example.web_service.entity.User;
import com.example.web_service.repository.FollowRepository;
import com.example.web_service.security.JwtUtil;
import com.example.web_service.service.PostService;
import com.example.web_service.service.UserService;
import com.example.web_service.repository.LikeRepository;
import com.example.web_service.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api")
public class PostController {

    @Autowired private PostService postService;
    @Autowired private UserService userService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private LikeRepository likeRepository;
    @Autowired private CommentRepository commentRepository;

    private final DateTimeFormatter ISO_UTC = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @GetMapping("/posts")
    public Response<List<Map<String, Object>>> getAllPosts(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);

            UUID userId = jwtUtil.extractUserId(token);

            List<Post> posts = postService.getAllPosts(userId);
            List<Map<String, Object>> out = new ArrayList<>();

            for (Post p : posts) {
                Map<String, Object> u = new HashMap<>();
                u.put("id", p.getUser().getId());
                u.put("username", p.getUser().getUsername());
                u.put("avatar", p.getUser().getAvatar());
                u.put("is_followed", p.getUser().isFollowed());

                Map<String, Object> pu = new HashMap<>();
                pu.put("id", p.getId());
                pu.put("user", u);
                pu.put("content", p.getContent());
                pu.put("media_url", p.getMediaUrl());
                pu.put("is_liked", p.isLiked());
                pu.put("likes_count", likeRepository.countByTargetId(p.getId()));
                pu.put("comments_count", commentRepository.countByPost(p));
                pu.put("created_at", p.getCreatedAt().atOffset(ZoneOffset.UTC).format(ISO_UTC));
                out.add(pu);
            }

            return Response.successfulResponse("All posts retrieved successfully!", out);
        } catch (Exception e) {
            return Response.failedResponse(e.getMessage());
        }
    }

    @GetMapping("/posts/{postId}")
    public Response<Map<String, Object>> getPostById(@RequestHeader("Authorization") String authHeader, @PathVariable String postId) {
        try {
            String token = authHeader.substring(7);

            UUID userId = jwtUtil.extractUserId(token);
            UUID postUUID = UUID.fromString(postId);

            Post post = postService.getPostById(postUUID, userId);

            Map<String, Object> u = new HashMap<>();
            u.put("id", post.getUser().getId());
            u.put("username", post.getUser().getUsername());
            u.put("avatar", post.getUser().getAvatar());
            u.put("is_followed", post.getUser().isFollowed());

            Map<String, Object> pu = new HashMap<>();
            pu.put("id", post.getId());
            pu.put("user", u);
            pu.put("content", post.getContent());
            pu.put("media_url", post.getMediaUrl());
            pu.put("is_liked", post.isLiked());
            pu.put("likes_count", likeRepository.countByTargetId(post.getId()));
            pu.put("comments_count", commentRepository.countByPost(post));
            pu.put("created_at", post.getCreatedAt().atOffset(ZoneOffset.UTC).format(ISO_UTC));

            return Response.successfulResponse("Post created successfully!", pu);
        } catch (Exception e) {
            return Response.failedResponse(e.getMessage());
        }
    }

    @GetMapping("/users/{username}/posts")
    public Response<List<Map<String, Object>>> getPostsByUsername(@PathVariable String username) {
        try {
            User user = userService.findByUsername(username);
            List<Post> posts = postService.getPostsByUser(user);
            List<Map<String, Object>> out = new ArrayList<>();

            for (Post p : posts) {
                Map<String, Object> u = new HashMap<>();
                u.put("id", p.getUser().getId());
                u.put("username", p.getUser().getUsername());
                u.put("avatar", p.getUser().getAvatar());
                u.put("is_followed", p.getUser().isFollowed());

                Map<String, Object> pu = new HashMap<>();
                pu.put("id", p.getId());
                pu.put("user", u);
                pu.put("content", p.getContent());
                pu.put("media_url", p.getMediaUrl());
                pu.put("is_liked", p.isLiked());
                pu.put("likes_count", likeRepository.countByTargetId(p.getId()));
                pu.put("comments_count", commentRepository.countByPost(p));
                pu.put("created_at", p.getCreatedAt().atOffset(ZoneOffset.UTC).format(ISO_UTC));
                out.add(pu);
            }

            return Response.successfulResponse("User posts retrieved successfully!", out);
        } catch (Exception e) {
            return Response.failedResponse(e.getMessage());
        }
    }

    @GetMapping("/users/{username}/posts/{postId}")
    public Response<Map<String, Object>> getPostDetailByUsername(@PathVariable String username, @PathVariable String postId) {
        try {
            UUID postUUID = UUID.fromString(postId);
            Post post = postService.getPostByUsernameAndId(username, postUUID);

            Map<String, Object> data = new HashMap<>();
            data.put("id", post.getId());
            data.put("content", post.getContent());
            data.put("media_url", post.getMediaUrl());
            data.put("likes_count", likeRepository.countByTargetId(post.getId()));
            data.put("comments_count", commentRepository.countByPost(post));
            data.put("created_at", post.getCreatedAt().atOffset(ZoneOffset.UTC).format(ISO_UTC));

            return Response.successfulResponse("User post detail retrieved successfully!", data);
        } catch (Exception e) {
            return Response.failedResponse(404, e.getMessage(), null);
        }
    }

    @GetMapping("/posts/self")
    public Response<List<Map<String, Object>>> getOwnPosts(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            UUID userId = jwtUtil.extractUserId(token);
            User user = userService.findById(userId);
            List<Post> posts = postService.getPostsByUser(user);

            List<Map<String, Object>> out = new ArrayList<>();
            for (Post p : posts) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", p.getId());
                item.put("content", p.getContent());
                item.put("media_url", p.getMediaUrl());
                item.put("created_at", p.getCreatedAt().atOffset(ZoneOffset.UTC).format(ISO_UTC));
                out.add(item);
            }

            return Response.successfulResponse("Your posts retrieved successfully!", out);
        } catch (Exception e) {
            return Response.failedResponse(e.getMessage());
        }
    }

    @GetMapping("/posts/self/{postId}")
    public Response<Map<String, Object>> getOwnPostDetail(@RequestHeader("Authorization") String authHeader, @PathVariable String postId) {
        try {
            String token = authHeader.substring(7);
            UUID userId = jwtUtil.extractUserId(token);
            UUID postUUID = UUID.fromString(postId);
            User user = userService.findById(userId);
            Post post = postService.getPostByUserAndId(user, postUUID);

            Map<String, Object> data = new HashMap<>();
            data.put("id", post.getId());
            data.put("content", post.getContent());
            data.put("media_url", post.getMediaUrl());
            data.put("likes_count", likeRepository.countByTargetId(post.getId()));
            data.put("comments_count", commentRepository.countByPost(post));
            data.put("created_at", post.getCreatedAt().atOffset(ZoneOffset.UTC).format(ISO_UTC));

            return Response.successfulResponse("Your post detail retrieved successfully!", data);
        } catch (Exception e) {
            return Response.failedResponse(e.getMessage());
        }
    }

    @PostMapping("/posts")
    public Response<Map<String, Object>> createPost(
            @RequestHeader(name = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> body) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Header Authorization tidak valid");
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                throw new RuntimeException("Token tidak valid/expired");
            }

            UUID userId = jwtUtil.extractUserId(token);
            User user = userService.findById(userId);

            String content = body.get("content");
            String media = body.get("media");
            Post post = postService.createPost(user, content, media);

            Map<String, Object> data = new HashMap<>();
            data.put("id", post.getId());
            data.put("content", post.getContent());
            data.put("media_url", post.getMediaUrl());
            data.put("created_at", post.getCreatedAt().atOffset(ZoneOffset.UTC).format(ISO_UTC));

            return Response.successfulResponse("Post created successfully!", data);
        } catch (Exception e) {
            return Response.failedResponse(e.getMessage());
        }
    }
}
