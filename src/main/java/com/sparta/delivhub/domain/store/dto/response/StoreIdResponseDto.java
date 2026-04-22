package com.sparta.delivhub.domain.store.dto.response;

import lombok.*;

import java.util.UUID;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StoreIdResponseDto {
    private UUID storeId;
}