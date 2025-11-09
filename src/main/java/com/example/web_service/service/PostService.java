package com.example.web_service.service;

import com.example.web_service.entity.Post;
import com.example.web_service.entity.User;
import com.example.web_service.repository.PostRepository;
import com.example.web_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {
    @Autowired private PostRepository postRepository;
    @Autowired private UserRepository userRepository;

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

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public List<Post> getPostsByUser(User user) {
        return postRepository.findByUser(user);
    }

    public Post getPostByUserAndId(User user, Long postId) {
        return postRepository.findByUserAndId(user, postId)
                .orElseThrow(() -> new RuntimeException("Post tidak ditemukan"));
    }

    public Post getPostByUsernameAndId(String username, Long postId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        return postRepository.findByUserAndId(user, postId)
                .orElseThrow(() -> new RuntimeException("Post tidak ditemukan"));
    }
}
