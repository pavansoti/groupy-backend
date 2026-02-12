package com.groupy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchDto {
    private Long id;
    private String username;
    private String email;
    private String bio;
    private String imageUrl;
    private long followerCount;
    private boolean isFollowing;
}