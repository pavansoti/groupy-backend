package com.groupy.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.groupy.dto.ApiResponse;
import com.groupy.dto.FollowCountDto;
import com.groupy.dto.PaginationResponse;
import com.groupy.dto.UserDto;
import com.groupy.entity.Follow;
import com.groupy.entity.User;
import com.groupy.service.FollowService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{id}/follow")
    public ResponseEntity<ApiResponse<String>> followUser(@PathVariable Long id) {
        Follow follow = followService.followUser(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Successfully followed user"));
    }

    @DeleteMapping("/{id}/unfollow")
    public ResponseEntity<ApiResponse<String>> unfollowUser(@PathVariable Long id) {
        followService.unfollowUser(id);
        return ResponseEntity.ok(ApiResponse.success("Successfully unfollowed user"));
    }

    @GetMapping("/{id}/followers")
    public ResponseEntity<ApiResponse<PaginationResponse>> getFollowers(
    		Principal principal,
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        PaginationResponse response = followService.getFollowers(principal.getName(), id, page, limit);

        return ResponseEntity.ok(
                ApiResponse.success("Followers retrieved successfully", response)
        );
    }

    @GetMapping("/{id}/following")
    public ResponseEntity<ApiResponse<PaginationResponse>> getFollowing(
    		Principal principal,
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        PaginationResponse response = followService.getFollowing(principal.getName(), id, page, limit);

        return ResponseEntity.ok(
                ApiResponse.success("Following users retrieved successfully", response)
        );
    }

    @GetMapping("/{id}/follow-stats")
    public ResponseEntity<ApiResponse<FollowCountDto>> getFollowStats(@PathVariable Long id) {
        Long followersCount = followService.getFollowersCount(id);
        Long followingCount = followService.getFollowingCount(id);
        Boolean isFollowing = followService.isFollowing(id);
        
        FollowCountDto followCountDto = new FollowCountDto(id, followersCount, followingCount, isFollowing);
        
        return ResponseEntity.ok(ApiResponse.success("Follow statistics retrieved successfully", followCountDto));
    }

}