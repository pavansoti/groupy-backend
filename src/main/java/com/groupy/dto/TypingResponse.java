package com.groupy.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TypingResponse {
    private String conversationId;
    private String userName;
    private boolean typing;
}
