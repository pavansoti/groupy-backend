package com.groupy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadReceiptResponse {
    private Long conversationId;
    private Long userId;
    private String username;
    private String type; // "READ"
}
