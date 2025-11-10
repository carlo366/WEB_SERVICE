package com.example.web_service.controller;

import com.example.web_service.service.FollowService;
import com.example.web_service.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/follows")
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private JwtUtil jwtUtil;

    // ini follow
    @PostMapping("/{userId}")
    public Map<String, Object> followUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String userId
    ) {
        Map<String, Object> resp = new HashMap<>();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Header Authorization tidak valid");
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                throw new RuntimeException("Token tidak valid atau sudah expired");
            }

            UUID followerId = jwtUtil.extractUserId(token);
            UUID userUUID=UUID.fromString(userId);
            followService.followUser(followerId, userUUID);

            resp.put("status_code", 200);
            resp.put("message", "Berhasil mengikuti user!");
            resp.put("success", true);
            resp.put("data", null);
        } catch (Exception e) {
            resp.put("status_code", 400);
            resp.put("message", e.getMessage());
            resp.put("success", false);
            resp.put("data", null);
        }
        return resp;
    }

    // ini unfollow
    @DeleteMapping("/{userId}")
    public Map<String, Object> unfollowUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String userId
    ) {
        Map<String, Object> resp = new HashMap<>();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Header Authorization tidak valid");
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                throw new RuntimeException("Token tidak valid atau sudah expired");
            }

            UUID followerId = jwtUtil.extractUserId(token);
            UUID userUUID=UUID.fromString(userId);
            followService.unfollowUser(followerId, userUUID);

            resp.put("status_code", 200);
            resp.put("message", "Berhasil berhenti mengikuti user!");
            resp.put("success", true);
            resp.put("data", null);
        } catch (Exception e) {
            resp.put("status_code", 400);
            resp.put("message", e.getMessage());
            resp.put("success", false);
            resp.put("data", null);
        }
        return resp;
    }
}
