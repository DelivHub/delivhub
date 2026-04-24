package com.sparta.delivhub.domain.review.dto;

import com.sparta.delivhub.domain.review.entity.Review;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class StoreReviewListResponseDto {
    private List<StoreReviewDto> content; // 명세서 규격에 맞춘 리스트 이름

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private String sort;

    public StoreReviewListResponseDto(Page<Review> reviewPage) {
        this.content = reviewPage.getContent().stream()
                .map(StoreReviewDto::new)
                .collect(Collectors.toList());

        this.page = reviewPage.getNumber();
        this.size = reviewPage.getSize();
        this.totalElements = reviewPage.getTotalElements();
        this.totalPages = reviewPage.getTotalPages();
        this.sort = reviewPage.getSort().toString();
    }
}