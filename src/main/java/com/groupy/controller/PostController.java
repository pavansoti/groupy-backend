package com.groupy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.groupy.dto.ApiResponse;
import com.groupy.dto.PaginationResponse;
import com.groupy.dto.PostResponseDto;
import com.groupy.service.PostService;

import java.security.Principal;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponseDto>> createPost(
            @RequestParam("caption") String caption,
            @RequestParam(required = false) MultipartFile file,
            Principal principal) {

        PostResponseDto post = postService.createPost(principal.getName(), caption, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Post created successfully", post));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id, Principal principal) {
        postService.deletePost(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Post deleted successfully"));
    }

    @GetMapping("/feed/following")
    public ResponseEntity<ApiResponse<PaginationResponse>> getFeed(
		@RequestParam Boolean onlyLiked,
		@RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int limit,
		Principal principal
	) {
    	PaginationResponse res = postService.getFeed(principal.getName(), onlyLiked, page, limit);
        return ResponseEntity.ok(ApiResponse.success("Feed retrieved successfully", res));
    }

    @GetMapping("/feeds/{userId}")
    public ResponseEntity<ApiResponse<PaginationResponse>> getFeedsByUsername(
		@PathVariable Long userId, 
		@RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int limit,
        Principal principal
    ) {
    	PaginationResponse feed = postService.getPostsByUser(userId, principal.getName(), page, limit);
        return ResponseEntity.ok(ApiResponse.success("Feed retrieved successfully", feed));
    }
    
    @GetMapping("/feeds/liked")
    public ResponseEntity<ApiResponse<PaginationResponse>> getLikedFeedsByUsername(
		@RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int limit,
		Principal principal
	) {
    	PaginationResponse feed = postService.getLikedPostsByUser(principal.getName(), page, limit);
        return ResponseEntity.ok(ApiResponse.success("Feed retrieved successfully", feed));
    }


    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<Void>> likePost(
            @PathVariable Long postId,
            Principal principal) {
        postService.likePost(postId, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Post liked successfully"));
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<Void>> unlikePost(
            @PathVariable Long postId,
            Principal principal) {
        postService.unlikePost(postId, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Post unliked successfully"));
    }
}
