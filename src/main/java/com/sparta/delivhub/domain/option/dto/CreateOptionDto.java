package com.sparta.delivhub.domain.option.dto;

import com.sparta.delivhub.domain.option.entity.OptionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOptionDto {
    @NotBlank(message = "옵션 그룹명은 필수입니다.")
    @Size(max = 100, message = "옵션 그룹명은 100자 이하여야 합니다.")
    private String name;

    @NotNull(message = "옵션 선택 방식은 필수입니다.")
    private OptionType type;

    @Valid
    @NotEmpty(message = "옵션 아이템은 최소 1개 이상 필요합니다.")
    private List<Item> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        @NotBlank(message = "옵션 아이템명은 필수입니다.")
        @Size(max = 100, message = "옵션 아이템명은 100자 이하여야 합니다.")
        private String name;

        @NotNull(message = "추가 금액은 필수입니다.")
        @Min(value = 0, message = "추가 금액은 0 이상이어야 합니다.")
        private Long extraPrice;
    }
}