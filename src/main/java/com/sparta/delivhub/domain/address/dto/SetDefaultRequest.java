package com.sparta.delivhub.domain.address.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SetDefaultRequest {

    @NotNull(message = "기본 배송지 여부는 필수입니다.")
    private Boolean isDefault;
}
