package com.sparta.delivhub.domain.order.service.dto;

import com.sparta.delivhub.domain.order.service.entity.Order;
import com.sparta.delivhub.domain.order.service.entity.OrderItemOption;
import com.sparta.delivhub.domain.order.service.entity.OrderStatus;
import com.sparta.delivhub.domain.order.service.entity.OrderItem;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class OrderResponseDto {
    private UUID orderId;
    private UUID storeId;
    private Long totalPrice;
    private OrderStatus status;
    private String request;
    private LocalDateTime createdAt;
    private List<OrderItemResponseDto> orderItems;

    public static OrderResponseDto from(Order order) {
        return OrderResponseDto.builder()
            .orderId(order.getId())
            .storeId(order.getStoreId())
            .totalPrice(order.getTotalPrice())
            .status(order.getStatus())
            .request(order.getRequest())
            .createdAt(order.getCreatedAt())
            .orderItems(order.getOrderItems().stream()
                .map(OrderItemResponseDto::from)
                .toList())
            .build();
    }

    @Getter
    @Builder
    public static class OrderItemResponseDto {
        private UUID menuId;
        private Integer quantity;
        private Long unitPrice;
        private List<OrderItemOptionResponseDto> options;

        public static OrderItemResponseDto from(OrderItem item) {
            return OrderItemResponseDto.builder()
                .menuId(item.getMenuId())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .options(item.getOptions().stream()
                    .map(OrderItemOptionResponseDto::from)
                    .toList())
                .build();
        }
    }
    @Getter
    @Builder
    public static class OrderItemOptionResponseDto {
        private UUID optionId;
        private String optionName;
        private UUID optionItemId;
        private String optionItemName;
        private Long extraPrice;

        public static OrderItemOptionResponseDto from(OrderItemOption option) {
            return OrderItemOptionResponseDto.builder()
                    .optionId(option.getOptionId())
                    .optionName(option.getOptionName())
                    .optionItemId(option.getOptionItemId())
                    .optionItemName(option.getOptionItemName())
                    .extraPrice(option.getExtraPrice())
                    .build();
        }
    }
}
