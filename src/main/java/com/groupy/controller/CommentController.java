package com.groupy.controller;
import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.groupy.dto.ApiResponse;
import com.groupy.dto.CommentDto;
import com.groupy.security.CustomUserDetails;
import com.groupy.service.CommentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{postId}")
    public ResponseEntity<ApiResponse<CommentDto>> createComment(@PathVariable Long postId,
                                 @RequestParam String comment,
                                 @AuthenticationPrincipal CustomUserDetails user) {

    	return ResponseEntity.ok(
        	ApiResponse.success("Comment created successfully", commentService.createComment(postId, user.getId(), comment))
        );
    }
    
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentDto>> updateComment(
            @PathVariable Long commentId,
            @RequestParam String comment,
            Principal principal) {

        CommentDto dto = commentService.updateComment(commentId, comment, principal);

        ApiResponse<CommentDto> response = ApiResponse.success("Comment updated successfully", dto);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<String>> deleteComment(@PathVariable Long commentId) {

        commentService.deleteComment(commentId);
        return ResponseEntity.ok(
        	ApiResponse.success("Comment deleted successfully")
        );
    }
    
}