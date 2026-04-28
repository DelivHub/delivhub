package com.sparta.delivhub.domain.option.dto;

import com.sparta.delivhub.domain.option.entity.Option;
import com.sparta.delivhub.domain.option.entity.OptionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ResponseOptionDto {
    private UUID optionId;
    private UUID menuId;
    private String name;
    private OptionType type;
    private List<Item> items;
    private LocalDateTime createdAt;
    private String createdBy;

    public static ResponseOptionDto from(Option option) {
        return ResponseOptionDto.builder()
                .optionId(option.getId())
                .menuId(option.getMenu().getId())
                .name(option.getName())
                .type(option.getType())
                .items(
                        option.getOptionItems().stream()
                                .filter(optionItem -> optionItem.getDeletedAt() == null)
                                .map(optionItem -> Item.builder()
                                        .optionItemId(optionItem.getId())
                                        .name(optionItem.getName())
                                        .extraPrice(optionItem.getExtraPrice())
                                        .build())
                                .toList()
                )
                .createdAt(option.getCreatedAt())
                .createdBy(option.getCreatedBy())
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Item {
        private UUID optionItemId;
        private String name;
        private Long extraPrice;
    }
}