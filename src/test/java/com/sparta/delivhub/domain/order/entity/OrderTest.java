package com.sparta.delivhub.domain.order.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    @DisplayName("주문 생성 시 초기 상태는 PENDING이어야 함")
    void initialState_IsPending() {
        // given
        Order order = Order.builder().build();

        // then
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    @DisplayName("주문 상태 전이 테스트 - 정상 흐름")
    void updateStatus_Success() {
        // given
        Order order = Order.builder().build();

        // when & then
        order.updateStatus(OrderStatus.ACCEPTED);
        assertEquals(OrderStatus.ACCEPTED, order.getStatus());

        order.updateStatus(OrderStatus.COOKING);
        assertEquals(OrderStatus.COOKING, order.getStatus());

        order.updateStatus(OrderStatus.DELIVERING);
        assertEquals(OrderStatus.DELIVERING, order.getStatus());

        order.updateStatus(OrderStatus.DELIVERED);
        assertEquals(OrderStatus.DELIVERED, order.getStatus());

        order.updateStatus(OrderStatus.COMPLETED);
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    @DisplayName("주문 상태 전이 테스트 - 역방향 변경 시 예외 발생")
    void updateStatus_Fail_Backward() {
        // given
        Order order = Order.builder().build();
        order.updateStatus(OrderStatus.ACCEPTED);

        // when & then
        assertThrows(IllegalStateException.class, () ->
                order.updateStatus(OrderStatus.PENDING)
        );
    }

    @Test
    @DisplayName("주문 상태 전이 테스트 - 종료된 상태(CANCELED)에서 변경 시 예외 발생")
    void updateStatus_Fail_FromCanceled() {
        // given
        Order order = Order.builder().build();
        order.cancel();

        // when & then
        assertThrows(IllegalStateException.class, () ->
                order.updateStatus(OrderStatus.ACCEPTED)
        );
    }

    @Test
    @DisplayName("주문 요청사항 수정 테스트 - PENDING 상태에서만 가능")
    void updateRequest_Success() {
        // given
        Order order = Order.builder().build();
        String newRequest = "맛있게 해주세요";

        // when
        order.updateRequest(newRequest);

        // then
        assertEquals(newRequest, order.getRequest());
    }

    @Test
    @DisplayName("주문 요청사항 수정 테스트 - ACCEPTED 이후 상태에서 수정 시 예외 발생")
    void updateRequest_Fail_AfterAccepted() {
        // given
        Order order = Order.builder().build();
        order.updateStatus(OrderStatus.ACCEPTED);

        // when & then
        assertThrows(IllegalStateException.class, () ->
                order.updateRequest("수정 시도")
        );
    }

    @Test
    @DisplayName("주문 아이템 추가 테스트")
    void addOrderItem_Success() {
        // given
        Order order = Order.builder().build();
        OrderItem item = OrderItem.builder()
                .menuId(UUID.randomUUID())
                .quantity(1)
                .unitPrice(10000L)
                .build();

        // when
        order.addOrderItem(item);

        // then
        assertEquals(1, order.getOrderItems().size());
        assertEquals(order, item.getOrder());
    }
}
