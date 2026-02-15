package com.groupy.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaginationResponse {

    private List<?> content;
    private int page;
    private int size;
    private boolean hasMore;
}