package com.groupy.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageResponse {

    private String id;
    private String conversationId;
    private String senderId;
    private String senderUsername;
    private String senderProfilePicture;
    private String content;
    private String type;
    private String createdAt;
    private boolean isRead;
}
