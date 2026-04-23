package com.sparta.delivhub.domain.category.dto.response;

import com.sparta.delivhub.domain.category.entity.Category;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CategoryNameResponseDto {
    private String name;

    public CategoryNameResponseDto(Category category) {
        this.name = category.getName();
    }
}