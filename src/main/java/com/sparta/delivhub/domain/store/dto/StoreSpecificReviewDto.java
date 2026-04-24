package com.sparta.delivhub.domain.store.dto;

import com.sparta.delivhub.domain.review.entity.Review;
import lombok.Getter;

import java.util.UUID;

@Getter
public class StoreSpecificReviewDto {
    private UUID reviewId;
    private String userNickname;
    private Integer rating;
    private String content;

    public StoreSpecificReviewDto(Review review) {
        this.reviewId = review.getId();
        // User 엔티티에 존재하는 nickname 필드 활용!
        this.userNickname = review.getUser().getNickname();
        this.rating = review.getRating();
        this.content = review.getContent();
    }
}