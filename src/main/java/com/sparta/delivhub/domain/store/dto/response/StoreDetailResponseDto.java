package com.sparta.delivhub.domain.store.dto.response;

import com.sparta.delivhub.domain.store.entity.Store;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StoreDetailResponseDto {
    private UUID storeId;
    private String name;
    private String address;
    private String number;
    private BigDecimal average_rating;

    public StoreDetailResponseDto(Store store) {
        this.storeId = store.getId();
        this.name = store.getName();
        this.address = store.getAddress();
        this.number = store.getNumber();
        this.average_rating = store.getAverageRating();
    }
}