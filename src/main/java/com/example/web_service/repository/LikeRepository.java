package com.example.web_service.repository;

import com.example.web_service.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {
    long countByTargetId(Long targetId);
}
