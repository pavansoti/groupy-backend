package com.instagram.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PostResponseDto {
    private Long id;
    private String caption;
    private String imageUrl;
    private LocalDateTime createdAt;
    private UserResponseDto user;
    private long likeCount;
    private boolean isLikedByCurrentUser;
}
