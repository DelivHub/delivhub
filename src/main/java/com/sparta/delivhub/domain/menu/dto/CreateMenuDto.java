package com.sparta.delivhub.domain.menu.dto;

import com.sparta.delivhub.domain.option.dto.CreateOptionDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class CreateMenuDto {
    @NotBlank(message = "메뉴 이름은 필수입니다.")
    @Size(max = 100, message = "메뉴 이름은 100자 이하여야 합니다.")
    private String name;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private Integer price;

    private String description;

    private Boolean aiDescription = false;

    @Size(max = 100, message = "프롬프트는 100자 이하여야 합니다.")
    private String aiPrompt;

    private List<CreateOptionDto> options = new ArrayList<>();
}
