package com.example.web_service.controller;

import com.example.web_service.entity.Comment;
import com.example.web_service.security.JwtUtil;
import com.example.web_service.service.PostInteractionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/posts")
public class PostInteractionController {

    @Autowired
    private PostInteractionService postInteractionService;

    @Autowired
    private JwtUtil jwtUtil;

    private UUID extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Token tidak ditemukan atau tidak valid");
        }
        return jwtUtil.extractUserId(token.substring(7));
    }

    private Map<String, Object> successResponse(String message, Object data) {
        Map<String, Object> res = new HashMap<>();
        res.put("status_code", 200);
        res.put("message", message);
        res.put("success", true);
        res.put("data", data);
        return res;
    }
    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> res = new HashMap<>();
        res.put("status_code", 400);
        res.put("message", message);
        res.put("success", false);
        res.put("data", null);
        return res;
    }

    // ini like a post
    @PostMapping("/{postId}/likes")
    public Map<String, Object> likePost(@PathVariable UUID postId, HttpServletRequest request) {
        try {
            UUID userId = extractUserId(request);
            String message = postInteractionService.likePost(postId, userId);
            Map<String, Object> data = Map.of("likes_count", postInteractionService.countLikes(postId));
            return successResponse(message, data);
        } catch (RuntimeException e) {
            return errorResponse(e.getMessage());
        }
    }

    // ini unlike
    @DeleteMapping("/{postId}/likes")
    public Map<String, Object> unlikePost(@PathVariable String postId, HttpServletRequest request) {
        try {
            UUID userId = extractUserId(request);
            UUID postUUID=UUID.fromString(postId);
            String message = postInteractionService.unlikePost(postUUID, userId);
            Map<String, Object> data = Map.of("likes_count", postInteractionService.countLikes(postUUID));
            return successResponse(message, data);
        } catch (RuntimeException e) {
            return errorResponse(e.getMessage());
        }
    }

    // ini tambah komentar
    @PostMapping("/{postId}/comments")
    public Map<String, Object> addComment(@PathVariable String postId,
                                          @RequestBody Map<String, String> body,
                                          HttpServletRequest request) {
        try {
            UUID userId = extractUserId(request);
            UUID postUUID=UUID.fromString(postId);
            String content = body.get("content");
            Comment comment = postInteractionService.addComment(postUUID, userId, content);

            Map<String, Object> data = new HashMap<>();
            data.put("comment_id", comment.getId());
            data.put("content", comment.getBody());
            return successResponse("Komentar berhasil ditambahkan!", data);
        } catch (RuntimeException e) {
            return errorResponse(e.getMessage());
        }
    }

    // ini hapus komentar
    @DeleteMapping("/comments/{commentId}")
    public Map<String, Object> deleteComment(@PathVariable String commentId, HttpServletRequest request) {
        try {
            UUID userId = extractUserId(request);
            UUID commentUUID=UUID.fromString(commentId);
            String message = postInteractionService.deleteComment(commentUUID, userId);
            return successResponse(message, null);
        } catch (RuntimeException e) {
            return errorResponse(e.getMessage());
        }
    }

    // ini all komentar
    @GetMapping("/{postId}/comments")
    public Map<String, Object> getComments(@PathVariable String postId) {
        try {
            UUID postUUID=UUID.fromString(postId);
            List<Comment> comments = postInteractionService.getComments(postUUID);
            return successResponse("Daftar komentar berhasil diambil!", comments);
        } catch (RuntimeException e) {
            return errorResponse(e.getMessage());
        }
    }
}
