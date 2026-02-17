package com.groupy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JoinEventResponse {

    private Long conversationId;
    private String username;
    private String type; // "JOIN"
}