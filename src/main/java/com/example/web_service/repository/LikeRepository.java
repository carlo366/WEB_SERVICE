package com.example.web_service.repository;

import com.example.web_service.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByUserIdAndTargetId(Long userId, Long targetId);
    long countByTargetId(Long targetId);
    Optional<Like> findByUserIdAndTargetId(Long userId, Long targetId); 
}
