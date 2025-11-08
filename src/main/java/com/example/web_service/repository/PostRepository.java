package com.example.web_service.repository;

import com.example.web_service.entity.Post;
import com.example.web_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUser(User user);
}
