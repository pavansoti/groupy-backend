package com.instagram.controller;

import com.instagram.dto.ApiResponse;
import com.instagram.dto.PostResponseDto;
import com.instagram.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

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
    public ResponseEntity<ApiResponse<List<PostResponseDto>>> getFeed(Principal principal) {
        List<PostResponseDto> feed = postService.getFeed(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Feed retrieved successfully", feed));
    }

    @GetMapping("/feeds/{userId}")
    public ResponseEntity<ApiResponse<List<PostResponseDto>>> getFeedsByUsername(@PathVariable Long userId, Principal principal) {
        List<PostResponseDto> feed = postService.getPostsByUser(userId, principal.getName());
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
