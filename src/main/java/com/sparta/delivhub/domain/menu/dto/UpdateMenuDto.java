package com.sparta.delivhub.domain.menu.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateMenuDto {
    @Size(max = 100, message = "메뉴 이름은 100자 이하여야 합니다.")
    private String name;

    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private Integer price;

    private String description;
}
