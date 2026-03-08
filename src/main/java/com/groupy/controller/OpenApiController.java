package com.groupy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.groupy.dto.ApiResponse;
import com.groupy.dto.PaginationResponse;
import com.groupy.dto.PostResponseDto;
import com.groupy.service.CommentService;
import com.groupy.service.PostService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/open")
@RequiredArgsConstructor
public class OpenApiController {

    private final PostService postService;
    private final CommentService commentService;
    
    @GetMapping("/comments/post/{id}")
    public ResponseEntity<ApiResponse<PaginationResponse>> getComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        PaginationResponse comments = commentService.getComments(id, page, limit);

        ApiResponse<PaginationResponse> response = ApiResponse.success("Comments fetched successfully", comments);

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/post/{postId}")
    public ResponseEntity<ApiResponse<PostResponseDto>> createPost(
        @PathVariable Long postId
    ) {

        PostResponseDto post = postService.getPostById(postId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Post retrived successfully", post));
    }
}
