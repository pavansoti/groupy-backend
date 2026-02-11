package com.groupy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationDto {
    private String type; // FOLLOW, LIKE
    private String message;
    private Long fromUserId;
    private String fromUsername;
    private Long targetId; // Post ID etc. if applicable
}
