package com.sparta.delivhub.domain.review.dto;

import com.sparta.delivhub.domain.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ReviewResponseDto {
    private UUID reviewId;
    private Integer rating;
    private String content;

    public ReviewResponseDto(Review review) {
        this.reviewId = review.getId();
        this.rating = review.getRating();
        this.content = review.getContent();
    }
}