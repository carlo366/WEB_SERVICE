package com.example.web_service.repository;

import com.example.web_service.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface LikeRepository extends JpaRepository<Like, UUID> {
    boolean existsByUserIdAndTargetId(UUID userId, UUID targetId);
    long countByTargetId(UUID targetId);
    Optional<Like> findByUserIdAndTargetId(UUID userId, UUID targetId);
}
