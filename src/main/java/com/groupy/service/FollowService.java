package com.groupy.service;

import java.util.List;

import org.hibernate.query.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.groupy.dto.PaginationResponse;
import com.groupy.dto.UserDto;
import com.groupy.dto.UserSearchDto;
import com.groupy.entity.Follow;
import com.groupy.entity.User;
import com.groupy.exception.ResourceNotFoundException;
import com.groupy.repository.FollowRepository;
import com.groupy.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public Follow followUser(Long followingId) {
        User authenticatedUser = getAuthenticatedUser();
        User followingUser = userRepository.findById(followingId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", followingId));

        // Prevent self-follow
        if (authenticatedUser.getId().equals(followingId)) {
            throw new IllegalArgumentException("You cannot follow yourself");
        }

        // Check if already following
        if (followRepository.existsByFollowerAndFollowing(authenticatedUser, followingUser)) {
            throw new IllegalArgumentException("You are already following this user");
        }

        Follow follow = Follow.builder()
                .follower(authenticatedUser)
                .following(followingUser)
                .build();

        Follow savedFollow = followRepository.save(follow);
        log.info("User {} started following user {}", authenticatedUser.getUsername(), followingUser.getUsername());

        // Send Notification
        notificationService.sendNotification(followingUser.getId(), com.groupy.dto.NotificationDto.builder()
                .type("FOLLOW")
                .message(authenticatedUser.getUsername() + " started following you")
                .fromUserId(authenticatedUser.getId())
                .fromUsername(authenticatedUser.getUsername())
                .build());

        return savedFollow;
    }

    public void unfollowUser(Long followingId) {
        User authenticatedUser = getAuthenticatedUser();
        User followingUser = userRepository.findById(followingId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", followingId));

        Follow follow = followRepository.findByFollowerAndFollowing(authenticatedUser, followingUser)
                .orElseThrow(() -> new ResourceNotFoundException("Follow relationship not found"));

        followRepository.delete(follow);
        log.info("User {} unfollowed user {}", authenticatedUser.getUsername(), followingUser.getUsername());
    }

    @Transactional(readOnly = true)
    public PaginationResponse getFollowers(String username, Long userId, int page, int limit) {
    	
    	User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Pageable pageable = PageRequest.of(
    		page, 
    		limit
        );

        Slice<User> followersPage = followRepository.findFollowersByUserId(userId, pageable);

        List<UserSearchDto> followerDtos = followersPage.getContent()
                .stream()
                .map(f -> this.convertToUserDto(f, user.getId()))
                .toList();

        boolean hasMore = followersPage.hasNext();

        return new PaginationResponse(
                followerDtos,
                page,
                limit,
                hasMore
        );
    }
    
    @Transactional(readOnly = true)
    public PaginationResponse getFollowing(String username, Long userId, int page, int limit) {
    	
    	User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Pageable pageable = PageRequest.of(
    		page, 
    		limit
        );
        
        List<User> testFollowing = followRepository.testFollowing(userId);
        
        System.out.println(testFollowing.size());

        Slice<User> followingPage = followRepository.findFollowingByUserId(userId, pageable);
        
        System.out.println(followingPage.getContent().size());

        List<UserSearchDto> followingDtos = followingPage.getContent()
                .stream()
                .map(f -> this.convertToUserDto(f, user.getId()))
                .toList();

        boolean hasMore = followingPage.hasNext();

        return new PaginationResponse(
                followingDtos,
                page,
                limit,
                hasMore
        );
    }

    @Transactional(readOnly = true)
    public Long getFollowersCount(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return followRepository.countFollowersByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Long getFollowingCount(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return followRepository.countFollowingByUserId(userId);
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Long userId) {
        User authenticatedUser = getAuthenticatedUser();
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return followRepository.existsByFollowerAndFollowing(authenticatedUser, targetUser);
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
    

    private UserSearchDto convertToUserDto(User user, Long currentUserId) {

    	boolean isFollowing = user.getFollowers() != null ? user.getFollowers().
                stream().
                anyMatch(follow -> follow.getFollower().getId().equals(currentUserId)) : false;

    	return new UserSearchDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                user.getImageUrl(),
                user.getFollowers() != null ? user.getFollowers().size() : 0,
                isFollowing
        );
    }
}