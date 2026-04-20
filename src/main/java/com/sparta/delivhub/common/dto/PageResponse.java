package com.sparta.delivhub.common.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;
import java.util.List;

@Getter
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public PageResponse(Page<T> pageInfo) {
        this.content = pageInfo.getContent();
        this.page = pageInfo.getNumber();
        this.size = pageInfo.getSize();
        this.totalElements = pageInfo.getTotalElements();
        this.totalPages = pageInfo.getTotalPages();
    }
}