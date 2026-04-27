package com.sparta.delivhub.domain.order.service.dto;

import com.sparta.delivhub.domain.order.service.entity.OrderType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDto {
    @NotNull(message = "가게 ID는 필수입니다.")
    private UUID storeId;

    @NotNull(message = "배송지 ID는 필수입니다.")
    private UUID addressId;

    @Builder.Default
    private OrderType orderType = OrderType.ONLINE;

    private String request;

    @NotEmpty(message = "주문 항목은 비어있을 수 없습니다.")
    private List<OrderItemRequestDto> items;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequestDto {
        @NotNull(message = "메뉴 ID는 필수입니다.")
        private UUID menuId;

        @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
        private Integer quantity;
    }
}
