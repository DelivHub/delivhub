package com.sparta.delivhub.domain.review.dto;

import com.sparta.delivhub.domain.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class MyReviewDto {
    private UUID reviewId;
    private String storeName;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;

    // 엔티티를 DTO로 변환
    public MyReviewDto(Review review) {
        this.reviewId = review.getId();
        this.storeName = review.getStore().getName();
        this.rating = review.getRating();
        this.content = review.getContent();
        this.createdAt = review.getCreatedAt();
    }
}
