package com.instagram.dto;

import lombok.Data;

@Data
public class UserResponseDto {
    
   private Long id;
   private String username;
   private String email;
   private String gender;
   private String bio;
   private String createdAt;
   private String profilePicUrl;
//    private String role;
   private long postCount;
   private long followerCount;
   private long followingCount;
   private boolean isPrivate;
   private boolean isFollowing;
//    private boolean isCurrentUser;
}