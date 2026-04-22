package com.sparta.delivhub.domain.option.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOptionDto {

    @NotBlank(message = "옵션명은 필수입니다.")
    @Size(max = 100, message = "옵션명은 100자 이하여야 합니다.")
    private String name;

    @NotNull(message = "추가 금액은 필수입니다.")
    @Min(value = 0, message = "추가 금액은 0 이상이어야 합니다.")
    private Long extraPrice;
}
