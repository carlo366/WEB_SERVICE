package com.example.web_service.repository;

import com.example.web_service.entity.Post;
import com.example.web_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUser(User user);
    Optional<Post> findByUserAndId(User user, Long id);
}
