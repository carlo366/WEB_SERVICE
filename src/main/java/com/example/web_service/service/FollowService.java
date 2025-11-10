package com.example.web_service.service;

import com.example.web_service.entity.Follow;
import com.example.web_service.entity.FollowId;
import com.example.web_service.repository.FollowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FollowService {

    @Autowired
    private FollowRepository followRepository;

    public void followUser(UUID followerId, UUID followeeId) {
        if (followerId.equals(followeeId)) {
            throw new RuntimeException("Tidak bisa mengikuti diri sendiri");
        }

        if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            throw new RuntimeException("Sudah mengikuti user ini");
        }

        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFolloweeId(followeeId);
        followRepository.save(follow);
    }

    public void unfollowUser(UUID followerId, UUID followeeId) {
        FollowId id = new FollowId(followerId, followeeId);
        if (!followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            throw new RuntimeException("Belum mengikuti user ini");
        }
        followRepository.deleteById(id);
    }
}
