package com.groupy.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConversationResponse {

    private String id;
    private String participantId;
    private String participantUsername;
    private String participantProfilePicture;
    private String lastMessage;
    private String lastMessageTime;
    private int unreadCount;
    private boolean isOnline;
}
