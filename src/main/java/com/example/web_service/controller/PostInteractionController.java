package com.example.web_service.controller;

import com.example.web_service.dto.Response;
import com.example.web_service.entity.Comment;
import com.example.web_service.entity.Post;
import com.example.web_service.security.JwtUtil;
import com.example.web_service.service.PostInteractionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/posts")
public class PostInteractionController {

    @Autowired
    private PostInteractionService postInteractionService;

    @Autowired
    private JwtUtil jwtUtil;

    private final DateTimeFormatter ISO_UTC = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private UUID extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Token tidak ditemukan atau tidak valid");
        }
        return jwtUtil.extractUserId(token.substring(7));
    }

    @PostMapping("/{postId}/likes")
    public Response<Map<String, Object>> toggleLike(@PathVariable UUID postId, HttpServletRequest request) {
        try {
            UUID userId = extractUserId(request);
            String message = postInteractionService.toggleLike(postId, userId);
            boolean isLiked = postInteractionService.isLikedByUser(postId, userId);

            Map<String, Object> data = Map.of("status", isLiked);
            return Response.successfulResponse(message, data);

        } catch (RuntimeException e) {
            return Response.failedResponse(e.getMessage());
        }
    }


    @PostMapping("/{postId}/comments")
    public Response<Map<String, Object>> addComment(@PathVariable UUID postId,
                                                    @RequestBody Map<String, String> body,
                                                    HttpServletRequest request) {
        try {
            UUID userId = extractUserId(request);
            String content = body.get("content");

            Comment comment = postInteractionService.addComment(postId, userId, content);

            Map<String, Object> data = new HashMap<>();
            data.put("comment_id", comment.getId());
            data.put("content", comment.getBody());
            return Response.successfulResponse("Komentar berhasil ditambahkan!", data);

        } catch (RuntimeException e) {
            return Response.failedResponse(e.getMessage());
        }
    }

   
    @DeleteMapping("/comments/{commentId}")
    public Response<Object> deleteComment(@PathVariable UUID commentId, HttpServletRequest request) {
        try {
            UUID userId = extractUserId(request);
            String message = postInteractionService.deleteComment(commentId, userId);
            return Response.successfulResponse(message);
        } catch (RuntimeException e) {
            return Response.failedResponse(e.getMessage());
        }
    }

    @GetMapping("/{postId}/comments")
    public Response<List<Map<String, Object>>> getComments(@PathVariable UUID postId, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);

            UUID userId = jwtUtil.extractUserId(token);

            List<Map<String, Object>> comments = postInteractionService.getComments(postId, userId);
            return Response.successfulResponse("Daftar komentar berhasil diambil!", comments);
        } catch (RuntimeException e) {
            return Response.failedResponse(e.getMessage());
        }
    }
}
