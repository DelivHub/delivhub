package com.sparta.delivhub.domain.review.dto;

import com.sparta.delivhub.domain.review.entity.Review;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class StoreReviewPageResponseDto {

    // 명세서 요구사항: 최상단에 평균 평점 노출
    private BigDecimal averageRating;

    // 리뷰 목록
    private List<StoreSpecificReviewDto> content;

    // 페이징 정보
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private String sort;

    // 생성자에서 가게의 평균 평점과 리뷰 페이지 데이터를 한 번에 받아서 조립합니다.
    public StoreReviewPageResponseDto(BigDecimal averageRating, Page<Review> reviewPage) {
        this.averageRating = averageRating;

        this.content = reviewPage.getContent().stream()
                .map(StoreSpecificReviewDto::new)
                .collect(Collectors.toList());

        this.page = reviewPage.getNumber();
        this.size = reviewPage.getSize();
        this.totalElements = reviewPage.getTotalElements();
        this.totalPages = reviewPage.getTotalPages();
        this.sort = reviewPage.getSort().toString();
    }
}
