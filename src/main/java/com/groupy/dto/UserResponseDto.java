package com.groupy.dto;

import com.groupy.enums.Gender;

import lombok.Data;

@Data
public class UserResponseDto {
    
   private Long id;
   private String username;
   private String email;
   private String bio;
   private Gender gender;
   private String createdAt;
   private String imageUrl;
//    private String role;
   private long postCount;
   private long followerCount;
   private long followingCount;
   private Boolean privateAccount;;
   private boolean isFollowing;
//    private boolean isCurrentUser;
}