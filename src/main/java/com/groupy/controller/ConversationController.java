package com.groupy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.groupy.dto.ApiResponse;
import com.groupy.dto.ConversationResponse;
import com.groupy.security.CustomUserDetails;
import com.groupy.service.ConversationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping("/{targetUserId}")
    public ResponseEntity<ApiResponse<?>> createOrGet(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long targetUserId
    ) {
        ConversationResponse conversation =
                conversationService.createOrGet(user.getId(), targetUserId);

        return ResponseEntity.ok(ApiResponse.success("Created",conversation));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getUserConversations(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ResponseEntity.ok(
        	ApiResponse.success("Total conversations", conversationService.getUserConversations(user.getId()))
        );
    }
}