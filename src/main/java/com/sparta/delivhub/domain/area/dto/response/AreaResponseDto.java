package com.sparta.delivhub.domain.area.dto.response;

import com.sparta.delivhub.domain.area.entity.Area;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AreaResponseDto {
    private String city;
    private String district;
    private String name;

    public AreaResponseDto(Area area) {
        this.city = area.getCity();
        this.district = area.getDistrict();
        this.name = area.getName();
    }
}
