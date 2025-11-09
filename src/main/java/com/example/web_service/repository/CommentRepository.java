package com.example.web_service.repository;

import com.example.web_service.entity.Comment;
import com.example.web_service.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    long countByPost(Post post);
    List<Comment> findByPost(Post post);
}
