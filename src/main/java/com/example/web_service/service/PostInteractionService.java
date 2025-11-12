package com.example.web_service.service;

import com.example.web_service.entity.*;
import com.example.web_service.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PostInteractionService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    private final DateTimeFormatter ISO_UTC = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    
    public String toggleLike(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post tidak ditemukan"));

        Like like = likeRepository.findByUserIdAndTargetId(userId, postId).orElse(null);

        if (like == null) {
            like = Like.builder()
                    .userId(userId)
                    .targetId(postId)
                    .status(true)
                    .build();
            likeRepository.save(like);
            return "Post disukai!";
        } else {
            // toggle status (true = like, false = unlike)
            like.setStatus(!like.getStatus());
            likeRepository.delete(like);
            return "Unlike berhasil!";
        }
    }

 
    public boolean isLikedByUser(UUID postId, UUID userId) {
        return likeRepository.findByUserIdAndTargetId(userId, postId)
                .map(Like::getStatus)
                .orElse(false);
    }

    public Comment addComment(UUID postId, UUID userId, String body) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post tidak ditemukan"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        if (body == null || body.trim().isEmpty()) {
            throw new RuntimeException("Isi komentar tidak boleh kosong");
        }

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .body(body.trim())
                .build();

        return commentRepository.save(comment);
    }

    
    public String deleteComment(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Komentar tidak ditemukan"));

        Post post = comment.getPost();
        UUID postOwnerId = post.getUser().getId();

        if (!comment.getUser().getId().equals(userId) && !postOwnerId.equals(userId)) {
            throw new RuntimeException("Kamu tidak memiliki izin untuk menghapus komentar ini");
        }

        commentRepository.delete(comment);
        return "Komentar berhasil dihapus!";
    }

    public List<Map<String, Object>> getComments(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post tidak ditemukan"));

        List<Comment> comments = commentRepository.findByPost(post);
        List<Map<String, Object>> out = new ArrayList<>();

        for (Comment comment : comments) {
            Map<String, Object> u = new HashMap<>();
            u.put("id", comment.getUser().getId());
            u.put("username", comment.getUser().getUsername());
            u.put("avatar", comment.getUser().getAvatar());
            u.put("is_followed", followRepository.existsByFollowerIdAndFolloweeId(userId, comment.getUser().getId()));

            Map<String, Object> p = new HashMap<>();
            p.put("id", post.getId());
            p.put("content", post.getContent());
            p.put("media_url", post.getMediaUrl());
            p.put("is_liked", likeRepository.existsByUserIdAndTargetId(userId, post.getId()));
            p.put("likes_count", likeRepository.countByTargetId(post.getId()));
            p.put("comments_count", commentRepository.countByPost(post));

            Map<String, Object> c = new HashMap<>();
            c.put("id", comment.getId());
            c.put("post", p);
            c.put("user", u);
            c.put("body", comment.getBody());
            c.put("created_at", comment.getCreatedAt().atOffset(ZoneOffset.UTC).format(ISO_UTC));

            out.add(c);
        }

        return out;
    }

}
