package com.groupy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserStatusResponse {

    private String username;
    private boolean online;
}