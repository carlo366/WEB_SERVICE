package com.example.web_service.controller;

import com.example.web_service.entity.Post;
import com.example.web_service.entity.User;
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

    // all post ini
    @GetMapping("/posts")
    public Map<String,Object> getAllPosts(){
        Map<String,Object> resp = new HashMap<>();
        try {
            List<Post> posts = postService.getAllPosts();
            List<Map<String,Object>> out = new ArrayList<>();
            for (Post p : posts){
                Map<String,Object> u = new HashMap<>();
                u.put("id", p.getUser().getId());
                u.put("username", p.getUser().getUsername());
                u.put("avatar", p.getUser().getAvatar());

                Map<String,Object> pu = new HashMap<>();
                pu.put("id", p.getId());
                pu.put("user", u);
                pu.put("content", p.getContent());
                pu.put("media_url", p.getMediaUrl());
                pu.put("likes_count", likeRepository.countByTargetId(p.getId()));
                pu.put("comments_count", commentRepository.countByPost(p));
                pu.put("created_at", p.getCreatedAt().atOffset(ZoneOffset.UTC).format(ISO_UTC));
                out.add(pu);
            }

            resp.put("status_code", 200);
            resp.put("message", "All posts retrieved successfully!");
            resp.put("success", true);
            resp.put("data", out);
        } catch (Exception e){
            resp.put("status_code", 400);
            resp.put("message", e.getMessage());
            resp.put("success", false);
        }
        return resp;
    }

    // ini post by username
    @GetMapping("/users/{username}/posts")
    public Map<String,Object> getPostsByUsername(@PathVariable String username){
        Map<String,Object> resp = new HashMap<>();
        try {
            User user = userService.findByUsername(username);
            List<Post> posts = postService.getPostsByUser(user);

            List<Map<String,Object>> out = new ArrayList<>();
            for (Post p : posts){
                Map<String,Object> item = new HashMap<>();
                item.put("id", p.getId());
                item.put("content", p.getContent());
                item.put("media_url", p.getMediaUrl());
                item.put("created_at", p.getCreatedAt().atOffset(ZoneOffset.UTC).format(ISO_UTC));
                out.add(item);
            }

            resp.put("status_code", 200);
            resp.put("message", "User posts retrieved successfully!");
            resp.put("success", true);
            resp.put("data", out);
        } catch (Exception e){
            resp.put("status_code", 400);
            resp.put("message", e.getMessage());
            resp.put("success", false);
        }
        return resp;
    }

    // ini get detil post by username
    @GetMapping("/users/{username}/posts/{postId}")
    public Map<String,Object> getPostDetailByUsername(@PathVariable String username, @PathVariable String postId) {
        Map<String,Object> resp = new HashMap<>();
        try {
            UUID postUUID=UUID.fromString(postId);
            Post post = postService.getPostByUsernameAndId(username, postUUID);

            Map<String,Object> data = new HashMap<>();
            data.put("id", post.getId());
            data.put("content", post.getContent());
            data.put("media_url", post.getMediaUrl());
            data.put("likes_count", likeRepository.countByTargetId(post.getId()));
            data.put("comments_count", commentRepository.countByPost(post));
            data.put("created_at", post.getCreatedAt().atOffset(ZoneOffset.UTC).format(ISO_UTC));

            resp.put("status_code", 200);
            resp.put("message", "User post detail retrieved successfully!");
            resp.put("success", true);
            resp.put("data", data);
        } catch (Exception e) {
            resp.put("status_code", 404);
            resp.put("message", e.getMessage());
            resp.put("success", false);
        }
        return resp;
    }

    // ini get post pribadi
    @GetMapping("/posts/self")
    public Map<String,Object> getOwnPosts(@RequestHeader("Authorization") String authHeader) {
        Map<String,Object> resp = new HashMap<>();
        try {
            String token = authHeader.substring(7);
            UUID userId = jwtUtil.extractUserId(token);
            User user = userService.findById(userId); 
            List<Post> posts = postService.getPostsByUser(user);

            List<Map<String,Object>> out = new ArrayList<>();
            for (Post p : posts){
                Map<String,Object> item = new HashMap<>();
                item.put("id", p.getId());
                item.put("content", p.getContent());
                item.put("media_url", p.getMediaUrl());
                item.put("created_at", p.getCreatedAt().atOffset(ZoneOffset.UTC).format(ISO_UTC));
                out.add(item);
            }

            resp.put("status_code", 200);
            resp.put("message", "Your posts retrieved successfully!");
            resp.put("success", true);
            resp.put("data", out);
        } catch (Exception e){
            resp.put("status_code", 400);
            resp.put("message", e.getMessage());
            resp.put("success", false);
        }
        return resp;
    }

    // detil post pribadi
    @GetMapping("/posts/self/{postId}")
    public Map<String,Object> getOwnPostDetail(@RequestHeader("Authorization") String authHeader, @PathVariable String postId) {
        Map<String,Object> resp = new HashMap<>();
        try {
            String token = authHeader.substring(7);
            UUID userId = jwtUtil.extractUserId(token);
            UUID postUUID=UUID.fromString(postId);
            User user = userService.findById(userId);   
            Post post = postService.getPostByUserAndId(user, postUUID);

            Map<String,Object> data = new HashMap<>();
            data.put("id", post.getId());
            data.put("content", post.getContent());
            data.put("media_url", post.getMediaUrl());
            data.put("likes_count", likeRepository.countByTargetId(post.getId()));
            data.put("comments_count", commentRepository.countByPost(post));
            data.put("created_at", post.getCreatedAt().atOffset(ZoneOffset.UTC).format(ISO_UTC));

            resp.put("status_code", 200);
            resp.put("message", "Your post detail retrieved successfully!");
            resp.put("success", true);
            resp.put("data", data);
        } catch (Exception e){
            resp.put("status_code", 400);
            resp.put("message", e.getMessage());
            resp.put("success", false);
        }
        return resp;
    }

    // ini membuat post
    @PostMapping("/posts")
    public Map<String,Object> createPost(
            @RequestHeader(name = "Authorization", required = false) String authHeader,
            @RequestBody Map<String,String> body){
        Map<String,Object> resp = new HashMap<>();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer "))
                throw new RuntimeException("Header Authorization tidak valid");

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token))
                throw new RuntimeException("Token tidak valid/expired");

            UUID userId = jwtUtil.extractUserId(token);
            User user = userService.findById(userId);

            String content = body.get("content");
            String media = body.get("media");
            Post post = postService.createPost(user, content, media);

            Map<String,Object> data = new HashMap<>();
            data.put("id", post.getId());
            data.put("content", post.getContent());
            data.put("media_url", post.getMediaUrl());
            data.put("created_at", post.getCreatedAt().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

            resp.put("status_code", 200);
            resp.put("message", "Post created successfully!");
            resp.put("success", true);
            resp.put("data", data);
        } catch (Exception e) {
            resp.put("status_code", 400);
            resp.put("message", e.getMessage());
            resp.put("success", false);
        }
        return resp;
    }
}
