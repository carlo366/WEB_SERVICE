package com.example.web_service.controller;

import com.example.web_service.dto.JwtResponse;
import com.example.web_service.dto.LoginRequest;
import com.example.web_service.dto.Response;
import com.example.web_service.entity.User;
import com.example.web_service.exception.ApplicationException;
import com.example.web_service.security.JwtUtil;
import com.example.web_service.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
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
    ) throws IOException {
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

    }

    // ✅ LOGIN
    @PostMapping("/login")
    public ResponseEntity<Response<JwtResponse>> login(@RequestBody @Valid LoginRequest body) {
            User user = userService.findByUsername(body.username());
            if (user==null){
                throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
            }
            if (passwordEncoder.matches(body.password(), user.getPasswordHash())) {
                String token = jwtUtil.generateToken(user.getId());

                return ResponseEntity.ok(Response.successfulResponse("Login Success",new JwtResponse(token)));
            } else {
                throw new ApplicationException(
                        HttpStatus.UNAUTHORIZED, 
                        "Invalid username or password",
                        null);
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
