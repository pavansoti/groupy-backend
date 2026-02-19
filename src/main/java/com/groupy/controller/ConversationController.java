package com.groupy.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.groupy.dto.ApiResponse;
import com.groupy.dto.ConversationResponse;
import com.groupy.security.CustomUserDetails;
import com.groupy.service.CloudinaryService;
import com.groupy.service.ConversationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final CloudinaryService cloudinaryService;
    
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

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file
    ) {

        try {

            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "File is empty"));
            }

            Map uploadResult = cloudinaryService.uploadFile(file);

            String secureUrl = uploadResult.get("secure_url").toString();
            String publicId = uploadResult.get("public_id").toString();
            String resourceType = uploadResult.get("resource_type").toString();

            return ResponseEntity.ok(Map.of(
                    "url", secureUrl,
                    "publicId", publicId,
                    "resourceType", resourceType
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Upload failed"));
        }
    }
}