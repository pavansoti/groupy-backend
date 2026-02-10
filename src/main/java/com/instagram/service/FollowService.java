package com.instagram.service;

import com.instagram.entity.Follow;
import com.instagram.entity.User;
import com.instagram.exception.ResourceNotFoundException;
import com.instagram.repository.FollowRepository;
import com.instagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        notificationService.sendNotification(followingUser.getId(), com.instagram.dto.NotificationDto.builder()
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
    public List<User> getFollowers(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return followRepository.findFollowersByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<User> getFollowing(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return followRepository.findFollowingByUserId(userId);
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
}