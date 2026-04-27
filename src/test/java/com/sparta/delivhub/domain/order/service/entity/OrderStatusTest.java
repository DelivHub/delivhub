package com.sparta.delivhub.domain.order.service.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTest {

    @Test
    @DisplayName("정상적인 상태 전이 확인")
    void canTransitionTo_Success() {
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.ACCEPTED));
        assertTrue(OrderStatus.ACCEPTED.canTransitionTo(OrderStatus.COOKING));
        assertTrue(OrderStatus.COOKING.canTransitionTo(OrderStatus.DELIVERING));
        assertTrue(OrderStatus.DELIVERING.canTransitionTo(OrderStatus.DELIVERED));
        assertTrue(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.COMPLETED));
    }

    @Test
    @DisplayName("역방향 상태 전이 방지 확인")
    void canTransitionTo_Fail_Backward() {
        assertFalse(OrderStatus.ACCEPTED.canTransitionTo(OrderStatus.PENDING));
        assertFalse(OrderStatus.COOKING.canTransitionTo(OrderStatus.ACCEPTED));
    }

    @Test
    @DisplayName("동일 상태 전이 방지 확인")
    void canTransitionTo_Fail_SameStatus() {
        assertFalse(OrderStatus.PENDING.canTransitionTo(OrderStatus.PENDING));
    }

    @Test
    @DisplayName("종료 상태(CANCELED, COMPLETED)에서 전이 방지 확인")
    void canTransitionTo_Fail_FromFinalStatus() {
        assertFalse(OrderStatus.CANCELED.canTransitionTo(OrderStatus.PENDING));
        assertFalse(OrderStatus.COMPLETED.canTransitionTo(OrderStatus.PENDING));
    }
}
