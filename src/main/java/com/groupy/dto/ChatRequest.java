package com.groupy.dto;

import lombok.Data;

@Data
public class ChatRequest {

    private Long conversationId;
    private String content;
    private String type; // text, file, audio
}
