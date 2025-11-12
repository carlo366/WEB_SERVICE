package com.example.web_service.dto;

import java.util.UUID;

import lombok.Builder;

@Builder
public record UserDto(
    UUID id,
    String username,
    String email,
    String avatar,
    String bio,
    boolean isFollowed
) {
    
}
