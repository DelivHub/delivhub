package com.sparta.delivhub.domain.menu.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HiddenMenuDto {
    @NotNull(message = "숨김 여부는 필수입니다.")
    private Boolean isHidden;
}
