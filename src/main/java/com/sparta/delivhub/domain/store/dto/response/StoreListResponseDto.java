package com.sparta.delivhub.domain.store.dto.response;

import com.sparta.delivhub.domain.store.entity.Store;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StoreListResponseDto {
    private UUID storeId;
    private String name;
    private BigDecimal averageRating;

    public StoreListResponseDto(Store store) {
        this.storeId = store.getId();
        this.name = store.getName();
        this.averageRating = store.getAverageRating();
    }
}