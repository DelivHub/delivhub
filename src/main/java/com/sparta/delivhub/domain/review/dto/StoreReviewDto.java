package com.sparta.delivhub.domain.review.dto;

import com.sparta.delivhub.domain.review.entity.Review;
import lombok.Getter;

import java.util.UUID;

@Getter
public class StoreReviewDto {
    private UUID reviewId;
    private String userId; // 닉네임 대신 식별 가능한 유저 ID 반환
    private Integer rating;
    private String content;

    public StoreReviewDto(Review review) {
        this.reviewId = review.getId();
        this.userId = review.getUser().getUsername(); // User 엔티티의 ID를 가져옴
        this.rating = review.getRating();
        this.content = review.getContent();
    }
}