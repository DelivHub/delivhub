package com.sparta.delivhub.domain.review.dto;

import com.sparta.delivhub.domain.review.entity.Review;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MyReviewListResponseDto {
    private String userId;
    private List<MyReviewDto> reviews;

    // 페이징 정보들
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private String sort;

    @Builder
    public MyReviewListResponseDto(String userId, Page<Review> reviewPage) {
        this.userId = userId;
        // Page<Review> 안의 엔티티들을 MyReviewDto로 싹 다 변환해서 리스트로 만듭니다.
        this.reviews = reviewPage.getContent().stream()
                .map(MyReviewDto::new)
                .collect(Collectors.toList());

        this.page = reviewPage.getNumber(); // 현재 페이지 번호 (0부터 시작)
        this.size = reviewPage.getSize();   // 페이지당 데이터 개수
        this.totalElements = reviewPage.getTotalElements(); // 전체 데이터 개수
        this.totalPages = reviewPage.getTotalPages();       // 전체 페이지 수
        this.sort = reviewPage.getSort().toString();        // 정렬 기준
    }
}