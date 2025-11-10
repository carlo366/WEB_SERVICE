package com.example.web_service.repository;

import com.example.web_service.entity.Follow;
import com.example.web_service.entity.FollowId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {
    boolean existsByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);
    long countByFolloweeId(UUID followeeId);
    long countByFollowerId(UUID followerId);
}
