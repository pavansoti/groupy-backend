package com.groupy.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentDto {

    private Long id;
    private Long postId;
    private Long userId;
    private String username;
    private String imageUrl;
    private String message;
    private LocalDateTime createdAt;
}