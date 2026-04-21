package com.sparta.delivhub.domain.option.dto;

import com.sparta.delivhub.domain.option.entity.Option;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ResponseOptionDto {
    private UUID optionId;
    private UUID menuId;
    private String name;
    private Long extraPrice;
    private LocalDateTime createdAt;
    private String createdBy;

    public static ResponseOptionDto from(Option option) {
        return ResponseOptionDto.builder()
                .optionId(option.getId())
                .menuId(option.getMenu().getId())
                .name(option.getName())
                .extraPrice(option.getExtraPrice())
                .createdAt(option.getCreatedAt())
                .createdBy(option.getCreatedBy())
                .build();
    }
}
