package com.groupy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowCountDto {
    private Long userId;
    private Long followersCount;
    private Long followingCount;
    private Boolean isFollowing;
}