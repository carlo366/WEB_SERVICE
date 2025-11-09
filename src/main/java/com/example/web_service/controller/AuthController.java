package com.example.web_service.controller;

import com.example.web_service.entity.User;
import com.example.web_service.security.JwtUtil;
import com.example.web_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Map<String, Object> successResponse(Object data, String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("status_code", 200);
        map.put("message", message);
        map.put("success", true);
        map.put("data", data);
        return map;
    }

    private Map<String, Object> errorResponse(String message, int code) {
        Map<String, Object> map = new HashMap<>();
        map.put("status_code", code);
        map.put("message", message);
        map.put("success", false);
        map.put("data", null);
        return map;
    }

    // ✅ REGISTER
    @PostMapping(value = "/register", consumes = { "multipart/form-data" })
    public Map<String, Object> register(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("email") String email,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            @RequestParam(value = "bio", required = false) String bio
    ) {
        try {
            if (username.isEmpty()) throw new RuntimeException("Username wajib diisi");
            if (password.isEmpty()) throw new RuntimeException("Password wajib diisi");
            if (email.isEmpty()) throw new RuntimeException("Email wajib diisi");

            User user = new User();
            user.setUsername(username);
            user.setPasswordHash(password);
            user.setEmail(email);
            user.setBio(bio != null ? bio : "");
            user.setCreatedAt(LocalDateTime.now());

            if (avatar != null && !avatar.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + avatar.getOriginalFilename();
                Path uploadPath = Paths.get("uploads");
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
                Files.copy(avatar.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                user.setAvatar("/uploads/" + fileName);
            }

            User savedUser = userService.register(user);
            savedUser.setPasswordHash(null);

            return successResponse(savedUser, "Register berhasil!");

        } catch (IOException e) {
            return errorResponse("Gagal upload avatar: " + e.getMessage(), 400);
        } catch (Exception e) {
            return errorResponse(e.getMessage(), 400);
        }
    }

    // ✅ LOGIN
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String password = body.get("password");

            if (username == null || username.isEmpty()) throw new RuntimeException("Username wajib diisi");
            if (password == null || password.isEmpty()) throw new RuntimeException("Password wajib diisi");

            User user = userService.findByUsername(username);

            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                String token = jwtUtil.generateToken(user.getId());
                long expiresIn = jwtUtil.getExpirationTime();
 
                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                data.put("expiresIn", expiresIn / 1000 + " detik");
                data.put("user", user);

                return successResponse(data, "Login berhasil!");
            } else {
                return errorResponse("Password salah!", 400);
            }
        } catch (Exception e) {
            return errorResponse(e.getMessage(), 400);
        }
    }

    // ✅ LOGOUT
    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer "))
                throw new RuntimeException("Token tidak valid");

            String token = authHeader.substring(7);

            if (jwtUtil.isTokenBlacklisted(token))
                throw new RuntimeException("Token sudah di-logout sebelumnya");

            jwtUtil.blacklistToken(token);
            return successResponse(null, "Logout berhasil, token di-blacklist");
        } catch (Exception e) {
            return errorResponse(e.getMessage(), 400);
        }
    }
}
