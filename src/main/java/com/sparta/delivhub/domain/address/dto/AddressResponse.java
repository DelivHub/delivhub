package com.sparta.delivhub.domain.address.dto;

import com.sparta.delivhub.domain.address.entity.Address;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AddressResponse {
    private UUID addressId;
    private String userId;
    private String alias;
    private String address;
    private String detail;
    private String zipCode;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AddressResponse from(Address address) {
        return AddressResponse.builder()
                .addressId(address.getAddressId())
                .userId(address.getUser().getUsername())
                .alias(address.getAlias())
                .address(address.getAddress())
                .detail(address.getDetail())
                .zipCode(address.getZipCode())
                .isDefault(address.getIsDefault())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}
