package com.sparta.delivhub.domain.menu.dto;

import com.sparta.delivhub.domain.menu.entity.Menu;
import com.sparta.delivhub.domain.option.dto.ResponseOptionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ResponseMenuDto {
    private UUID menuId;
    private UUID storeId;
    private String name;
    private Integer price;
    private String description;
    private boolean isHidden;
    private List<ResponseOptionDto> options;
    private LocalDateTime createdAt;
    private String createdBy;

    public static ResponseMenuDto from(Menu menu, List<ResponseOptionDto> options) {
        return ResponseMenuDto.builder()
                .menuId(menu.getId())
                .storeId(menu.getStore().getId())
                .name(menu.getName())
                .price(menu.getPrice())
                .description(menu.getDescription())
                .isHidden(menu.isHidden())
                .options(options)
                .createdAt(menu.getCreatedAt())
                .createdBy(menu.getCreatedBy())
                .build();
    }
}
