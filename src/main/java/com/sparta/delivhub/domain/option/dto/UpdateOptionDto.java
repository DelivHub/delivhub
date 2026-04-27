package com.sparta.delivhub.domain.option.dto;

import com.sparta.delivhub.domain.option.entity.OptionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOptionDto {
    @Size(max = 100, message = "옵션 그룹명은 100자 이하여야 합니다.")
    private String name;

    private OptionType type;

    @Valid
    private List<Item> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private UUID optionItemId;

        @Size(max = 100, message = "옵션 아이템명은 100자 이하여야 합니다.")
        private String name;

        @Min(value = 0, message = "추가 금액은 0 이상이어야 합니다.")
        private Long extraPrice;
    }
}