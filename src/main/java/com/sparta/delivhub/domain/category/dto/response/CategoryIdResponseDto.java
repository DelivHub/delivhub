package com.sparta.delivhub.domain.category.dto.response;

import lombok.*;

import java.util.UUID;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CategoryIdResponseDto {
    private UUID categoryId;
}