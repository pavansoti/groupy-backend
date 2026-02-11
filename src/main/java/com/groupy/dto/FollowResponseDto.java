package com.groupy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowResponseDto {
    private Long id;
    private UserDto follower;
    private UserDto following;
    private LocalDateTime createdAt;
}