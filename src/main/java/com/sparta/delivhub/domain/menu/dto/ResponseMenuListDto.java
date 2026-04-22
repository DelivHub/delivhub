package com.sparta.delivhub.domain.menu.dto;

import com.sparta.delivhub.domain.menu.entity.Menu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ResponseMenuListDto {
    private UUID menuId;
    private UUID storeId;
    private String name;
    private Integer price;
    private String description;
    private boolean isHidden;
    private LocalDateTime createdAt;

    public static ResponseMenuListDto from(Menu menu) {
        return ResponseMenuListDto.builder()
                .menuId(menu.getId())
                .storeId(menu.getStore().getId())
                .name(menu.getName())
                .price(menu.getPrice())
                .description(menu.getDescription())
                .isHidden(menu.isHidden())
                .createdAt(menu.getCreatedAt())
                .build();
    }
}
