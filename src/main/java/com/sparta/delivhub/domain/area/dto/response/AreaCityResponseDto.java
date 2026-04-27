package com.sparta.delivhub.domain.area.dto.response;

import com.sparta.delivhub.domain.area.entity.Area;
import lombok.*;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AreaCityResponseDto {
    private String city;

    public AreaCityResponseDto(Area area) {
        this.city = area.getCity();
    }
}