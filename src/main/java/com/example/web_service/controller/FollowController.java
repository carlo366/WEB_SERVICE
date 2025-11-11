package com.example.web_service.controller;

import com.example.web_service.dto.Response;
import com.example.web_service.security.JwtUtil;
import com.example.web_service.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/follows")
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private JwtUtil jwtUtil;

    // === FOLLOW USER ===
    @PostMapping("/{userId}")
    public Response<Void> followUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String userId
    ) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Header Authorization tidak valid");
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                throw new RuntimeException("Token tidak valid atau sudah expired");
            }

            UUID followerId = jwtUtil.extractUserId(token);
            UUID userUUID = UUID.fromString(userId);
            followService.followUser(followerId, userUUID);

            return Response.successfulResponse("Berhasil mengikuti user!");
        } catch (Exception e) {
            return Response.failedResponse(e.getMessage());
        }
    }

    // === UNFOLLOW USER ===
    @DeleteMapping("/{userId}")
    public Response<Void> unfollowUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String userId
    ) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Header Authorization tidak valid");
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                throw new RuntimeException("Token tidak valid atau sudah expired");
            }

            UUID followerId = jwtUtil.extractUserId(token);
            UUID userUUID = UUID.fromString(userId);
            followService.unfollowUser(followerId, userUUID);

            return Response.successfulResponse("Berhasil berhenti mengikuti user!");
        } catch (Exception e) {
            return Response.failedResponse(e.getMessage());
        }
    }
}
