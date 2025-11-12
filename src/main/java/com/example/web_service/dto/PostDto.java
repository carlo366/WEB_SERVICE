package com.example.web_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;

@Builder
public record PostDto(
    UUID id,
    UserDto user,
    String content,
    String mediaUrl,
    LocalDateTime createdAt,
    boolean isLiked,
    long likesCount,
    long commentsCount
) {
    
}
