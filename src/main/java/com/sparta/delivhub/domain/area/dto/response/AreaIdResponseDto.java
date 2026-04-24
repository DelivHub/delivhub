package com.sparta.delivhub.domain.area.dto.response;

import lombok.*;

import java.util.UUID;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AreaIdResponseDto {
    private UUID areaId;
}