package com.example.web_service.controller;

import com.example.web_service.entity.User;
import com.example.web_service.security.JwtUtil;
import com.example.web_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public Map<String, Object> getProfile(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> res = new HashMap<>();
        try {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            User user = userService.findByUsername(username);
            user.setPasswordHash(null);
            res.put("status", "success");
            res.put("user", user);
        } catch (Exception e) {
            res.put("status", "error");
            res.put("message", e.getMessage());
        }
        return res;
    }

    @PutMapping(consumes = { "multipart/form-data" })
    public Map<String, Object> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar
    ) {
        Map<String, Object> res = new HashMap<>();
        try {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            User user = userService.findByUsername(username);

            if (email != null) user.setEmail(email);
            if (bio != null) user.setBio(bio);
            if (avatar != null && !avatar.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + avatar.getOriginalFilename();
                Path uploadPath = Paths.get("uploads");
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
                Files.copy(avatar.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                user.setAvatar("/uploads/" + fileName);
            }

            User updated = userService.saveProfile(user);
            updated.setPasswordHash(null);
            res.put("status", "success");
            res.put("user", updated);
        } catch (Exception e) {
            res.put("status", "error");
            res.put("message", e.getMessage());
        }
        return res;
    }
}
