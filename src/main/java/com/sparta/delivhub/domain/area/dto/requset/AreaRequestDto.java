package com.sparta.delivhub.domain.area.dto.requset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class AreaRequestDto {

    @NotBlank(message = "시/도 이름은 필수입니다.")
    private String city;

    @NotBlank(message = "군/구 이름은 필수입니다.")
    private String district;

    @NotBlank(message = "주소는 필수입니다.")
    private String name;

    @NotNull(message = "공개 여부는 필수입니다.")
    private Boolean isHidden = false;

    public AreaRequestDto(String city, String district, String name) {
        this.name = name;
        this.city = city;
        this.district = district;
    }
}
