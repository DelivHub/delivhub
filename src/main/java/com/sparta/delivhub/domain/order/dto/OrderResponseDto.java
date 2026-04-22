package com.sparta.delivhub.domain.order.dto;

import com.sparta.delivhub.domain.order.entity.Order;
import com.sparta.delivhub.domain.order.entity.OrderStatus;
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

        public static OrderItemResponseDto from(com.sparta.delivhub.domain.order.entity.OrderItem item) {
            return OrderItemResponseDto.builder()
                .menuId(item.getMenuId())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .build();
        }
    }
}
