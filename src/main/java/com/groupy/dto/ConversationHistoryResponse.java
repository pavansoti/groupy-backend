package com.groupy.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationHistoryResponse {

    private String conversationId;
    private List<MessageResponse> messages;
    private String type; // "HISTORY"
}
