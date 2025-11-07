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

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // GET profile
    @GetMapping
    public Map<String, Object> getProfile(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Header Authorization tidak valid");
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                throw new RuntimeException("Token tidak valid / sudah expired");
            }

            String username = jwtUtil.extractUsername(token);
            User user = userService.findByUsername(username);
            user.setPasswordHash(null); // jangan kirim hash password

            response.put("status", "success");
            response.put("user", user);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Terjadi kesalahan: " + e.getMessage());
        }
        return response;
    }

    // UPDATE profile (username tidak bisa diubah)
    @PutMapping(consumes = { "multipart/form-data" })
    public Map<String, Object> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("status", "error");
                response.put("message", "Header Authorization tidak valid");
                return response;
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                response.put("status", "error");
                response.put("message", "Token tidak valid / sudah expired");
                return response;
            }

            String username = jwtUtil.extractUsername(token);
            User existingUser = userService.findByUsername(username);

            if (existingUser == null) {
                response.put("status", "error");
                response.put("message", "User tidak ditemukan");
                return response;
            }

            // Update field yang ada di entity
            if (email != null && email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                existingUser.setEmail(email);
            }
            if (bio != null) {
                existingUser.setBio(bio);
            }

            // Update avatar
            if (avatar != null && !avatar.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + avatar.getOriginalFilename();
                Path uploadPath = Paths.get("uploads");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Files.copy(avatar.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                existingUser.setAvatar("/uploads/" + fileName);
            }

            User updatedUser = userService.saveProfile(existingUser);
            updatedUser.setPasswordHash(null); // jangan kirim hash password

            response.put("status", "success");
            response.put("user", updatedUser);

        } catch (IOException e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Terjadi kesalahan saat upload file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Terjadi kesalahan: " + e.getMessage());
        }

        return response;
    }
}
