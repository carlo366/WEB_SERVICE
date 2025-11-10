package com.example.web_service.service;

import com.example.web_service.entity.*;
import com.example.web_service.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PostInteractionService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    public String likePost(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post tidak ditemukan"));

        boolean alreadyLiked = likeRepository.existsByUserIdAndTargetId(userId, postId);
        if (alreadyLiked) {
            throw new RuntimeException("Kamu sudah menyukai postingan ini");
        }

        Like like = Like.builder()
                .userId(userId)
                .targetId(postId)
                .build();

        likeRepository.save(like);
        return "Post disukai!";
    }

    public String unlikePost(UUID postId, UUID userId) {
        Like like = likeRepository.findByUserIdAndTargetId(userId, postId)
                .orElseThrow(() -> new RuntimeException("Kamu belum menyukai postingan ini"));

        likeRepository.delete(like);
        return "Unlike berhasil!";
    }

    public Comment addComment(UUID postId, UUID userId, String body) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post tidak ditemukan"));

        if (body == null || body.trim().isEmpty())
            throw new RuntimeException("Isi komentar tidak boleh kosong");

        Comment comment = Comment.builder()
                .post(post)
                .userId(userId)
                .body(body.trim())
                .build();

        return commentRepository.save(comment);
    }

    public String deleteComment(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Komentar tidak ditemukan"));

        Post post = comment.getPost();
        UUID postOwnerId = post.getUser().getId();

        if (!comment.getUserId().equals(userId) && !postOwnerId.equals(userId)) {
            throw new RuntimeException("Kamu tidak memiliki izin untuk menghapus komentar ini");
        }

        commentRepository.delete(comment);
        return "Komentar berhasil dihapus!";
    }

    public long countLikes(UUID postId) {
        return likeRepository.countByTargetId(postId);
    }

    public List<Comment> getComments(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post tidak ditemukan"));
        return commentRepository.findByPost(post);
    }
}
