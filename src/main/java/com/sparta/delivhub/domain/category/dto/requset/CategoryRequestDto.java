package com.sparta.delivhub.domain.category.dto.requset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@Getter
public class CategoryRequestDto {

    @NotBlank(message = "카테고리 이름은 필수입니다.")
    private String name;

    @NotNull(message = "공개 여부는 필수입니다.")
    private Boolean isActive = true;

    public CategoryRequestDto(String name) {
        this.name = name;

    }
}
