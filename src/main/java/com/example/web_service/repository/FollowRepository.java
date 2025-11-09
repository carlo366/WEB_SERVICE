package com.example.web_service.repository;

import com.example.web_service.entity.Follow;
import com.example.web_service.entity.FollowId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {
    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
    long countByFolloweeId(Long followeeId);
    long countByFollowerId(Long followerId);
}
