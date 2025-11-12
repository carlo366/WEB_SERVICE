package com.example.web_service.service;

import com.example.web_service.entity.Post;
import com.example.web_service.entity.User;
import com.example.web_service.repository.FollowRepository;
import com.example.web_service.repository.LikeRepository;
import com.example.web_service.repository.PostRepository;
import com.example.web_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PostService {
    @Autowired private PostRepository postRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private LikeRepository likeRepository;
    @Autowired private FollowRepository followRepository;

    public Post createPost(User user, String content, String mediaUrl) {
        if (user == null) throw new RuntimeException("User tidak ditemukan");
        if (content == null || content.trim().isEmpty()) throw new RuntimeException("Konten tidak boleh kosong");
        Post post = Post.builder()
                .user(user)
                .content(content.trim())
                .mediaUrl(mediaUrl)
                .createdAt(LocalDateTime.now())
                .build();
        return postRepository.save(post);
    }

    public List<Post> getAllPosts(UUID userId) {
        List<Post> posts = postRepository.findAll();

        for (Post post : posts) {
            boolean liked = false;
            boolean followed = false;

            if (userId != null) {
                liked = likeRepository.existsByUserIdAndTargetId(userId, post.getId());
                followed = followRepository.existsByFollowerIdAndFolloweeId(userId, post.getUser().getId());
            }
            post.getUser().setFollowed(followed);
            post.setLiked(liked);
        }

        return posts;
    }

    public Post getPostById(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post tidak ditemukan"));

        boolean liked = false;
        boolean followed = false;

        if (userId != null) {
            liked = likeRepository.existsByUserIdAndTargetId(userId, post.getId());
            followed = followRepository.existsByFollowerIdAndFolloweeId(userId, post.getUser().getId());
        }
        post.getUser().setFollowed(followed);
        post.setLiked(liked);

        return post;
    }

    public List<Post> getPostsByUser(User user) {
        return postRepository.findByUser(user);
    }

    public Post getPostByUserAndId(User user, UUID postId) {
        return postRepository.findByUserAndId(user, postId)
                .orElseThrow(() -> new RuntimeException("Post tidak ditemukan"));
    }

    public Post getPostByUsernameAndId(String username, UUID postId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        return postRepository.findByUserAndId(user, postId)
                .orElseThrow(() -> new RuntimeException("Post tidak ditemukan"));
    }
}
