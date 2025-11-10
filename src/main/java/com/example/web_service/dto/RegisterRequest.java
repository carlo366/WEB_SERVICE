package com.example.web_service.dto;

import org.springframework.web.multipart.MultipartFile;

public record RegisterRequest(
        String email,
        String username,
        String password,
        MultipartFile avatar,
        String bio
) {
}
