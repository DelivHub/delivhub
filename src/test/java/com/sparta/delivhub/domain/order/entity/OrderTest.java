package com.sparta.delivhub.domain.order.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    @DisplayName("주문 상태 전이 테스트 - 정상 흐름 (PENDING -> ACCEPTED)")
    void updateStatus_Success() {
        // given
        Order order = Order.builder()
                .status(OrderStatus.PENDING)
                .build();

        // when
        order.updateStatus(OrderStatus.ACCEPTED);

        // then
        assertEquals(OrderStatus.ACCEPTED, order.getStatus());
    }

    @Test
    @DisplayName("주문 상태 전이 테스트 - 역방향 변경 시 예외 발생")
    void updateStatus_Fail_Backward() {
        // given
        Order order = Order.builder().build();
        order.updateStatus(OrderStatus.ACCEPTED);
        order.updateStatus(OrderStatus.COOKING);

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
}
