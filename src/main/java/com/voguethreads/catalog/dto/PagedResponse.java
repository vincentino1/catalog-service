package com.voguethreads.catalog.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponse<T> {

    private List<T> items;
    private Integer page;
    private Integer pageSize;
    private Long totalItems;
    private Integer totalPages;
}

