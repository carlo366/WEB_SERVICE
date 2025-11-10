package com.example.web_service.controller;

import com.example.web_service.entity.User;
import com.example.web_service.security.JwtUtil;
import com.example.web_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // ini get profil
    @GetMapping
    public Map<String, Object> getProfile(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> resp = new HashMap<>();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Header Authorization tidak valid");
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                throw new RuntimeException("Token tidak valid atau sudah expired");
            }

            UUID userId = jwtUtil.extractUserId(token);
            User user = userService.findById(userId);
            user.setPasswordHash(null);

            long followersCount = userService.countFollowers(user.getId());
            long followingCount = userService.countFollowing(user.getId());

            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("email", user.getEmail());
            data.put("bio", user.getBio());
            data.put("avatar", user.getAvatar());
            data.put("followers", followersCount);
            data.put("following", followingCount);

            resp.put("status_code", 200);
            resp.put("message", "Successful!");
            resp.put("success", true);
            resp.put("data", data);

        } catch (Exception e) {
            resp.put("status_code", 400);
            resp.put("message", e.getMessage());
            resp.put("success", false);
            resp.put("data", null);
        }

        return resp;
    }

    // ini update profil
    @PutMapping(consumes = {"multipart/form-data"})
    public Map<String, Object> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar
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

            UUID userId = jwtUtil.extractUserId(token);
            User existingUser = userService.findById(userId);

            if (username != null && !username.isEmpty()) {
                if (!username.equals(existingUser.getUsername()) && userService.existsByUsername(username)) {
                    throw new RuntimeException("Username sudah digunakan!");
                }
                existingUser.setUsername(username);
            }

            if (email != null && !email.isEmpty()) {
                if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                    throw new RuntimeException("Format email tidak valid");
                }
                existingUser.setEmail(email);
            }

            if (bio != null && !bio.isEmpty()) {
                existingUser.setBio(bio);
            }

            if (avatar != null && !avatar.isEmpty()) {
                String uploadDir = "uploads/avatars";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

                String fileName = System.currentTimeMillis() + "_" + avatar.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(avatar.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                existingUser.setAvatar("/" + uploadDir + "/" + fileName);
            }

            User updated = userService.saveProfile(existingUser);
            updated.setPasswordHash(null);

            Map<String, Object> data = new HashMap<>();
            data.put("id", updated.getId());
            data.put("username", updated.getUsername());
            data.put("email", updated.getEmail());
            data.put("bio", updated.getBio());
            data.put("avatar", updated.getAvatar());

            resp.put("status_code", 200);
            resp.put("message", "Profil berhasil diperbarui");
            resp.put("success", true);
            resp.put("data", data);

        } catch (IOException e) {
            resp.put("status_code", 400);
            resp.put("message", "Gagal upload file: " + e.getMessage());
            resp.put("success", false);
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
