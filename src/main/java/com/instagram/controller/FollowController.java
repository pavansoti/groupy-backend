package com.instagram.controller;

import com.instagram.dto.ApiResponse;
import com.instagram.dto.FollowCountDto;
import com.instagram.dto.UserDto;
import com.instagram.entity.Follow;
import com.instagram.entity.User;
import com.instagram.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseEntity<ApiResponse<List<UserDto>>> getFollowers(@PathVariable Long id) {
        List<User> followers = followService.getFollowers(id);
        List<UserDto> followerDtos = followers.stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Followers retrieved successfully", followerDtos));
    }

    @GetMapping("/{id}/following")
    public ResponseEntity<ApiResponse<List<UserDto>>> getFollowing(@PathVariable Long id) {
        List<User> following = followService.getFollowing(id);
        List<UserDto> followingDtos = following.stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Following users retrieved successfully", followingDtos));
    }

    @GetMapping("/{id}/follow-stats")
    public ResponseEntity<ApiResponse<FollowCountDto>> getFollowStats(@PathVariable Long id) {
        Long followersCount = followService.getFollowersCount(id);
        Long followingCount = followService.getFollowingCount(id);
        Boolean isFollowing = followService.isFollowing(id);
        
        FollowCountDto followCountDto = new FollowCountDto(id, followersCount, followingCount, isFollowing);
        
        return ResponseEntity.ok(ApiResponse.success("Follow statistics retrieved successfully", followCountDto));
    }

    private UserDto convertToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setBio(user.getBio());
        dto.setCreatedAt(user.getCreatedAt().toString());
        return dto;
    }
}