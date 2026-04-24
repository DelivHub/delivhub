package com.sparta.delivhub.domain.review.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequestDto {

    @NotNull(message = "주문 ID는 필수입니다.")
    private UUID orderId;

    @NotNull(message = "가게 ID는 필수입니다.")
    private UUID storeId;

    @NotNull(message = "별점은 필수입니다.")
    @Min(value = 1, message = "별점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "별점은 5점 이하이어야 합니다.")
    private Integer rating;

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    @Size(max = 100, message = "리뷰 내용은 최대 100자까지 작성 가능합니다.")
    private String content;

    private String imageUrl; // 이미지는 필수가 아닐 수 있으므로 Validation을 걸지 않음
}