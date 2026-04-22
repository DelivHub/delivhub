package com.sparta.delivhub.domain.order.service;

import com.sparta.delivhub.domain.order.dto.OrderRequestDto;
import com.sparta.delivhub.domain.order.dto.OrderResponseDto;
import com.sparta.delivhub.domain.order.entity.Order;
import com.sparta.delivhub.domain.order.entity.OrderStatus;
import com.sparta.delivhub.domain.order.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Test
    @DisplayName("주문 취소 테스트 - 5분 이내 취소 성공")
    void cancelOrder_Success() {
        // given
        UUID orderId = UUID.randomUUID();
        String userId = "user01";
        Order order = mock(Order.class);
        
        given(orderRepository.findByIdAndDeletedAtIsNull(orderId)).willReturn(Optional.of(order));
        given(order.getUserId()).willReturn(userId);
        given(order.getCreatedAt()).willReturn(LocalDateTime.now().minusMinutes(3)); // 3분 전 생성

        // when
        orderService.cancelOrder(orderId, userId, "CUSTOMER");

        // then
        org.mockito.Mockito.verify(order).cancel();
    }

    @Test
    @DisplayName("주문 취소 테스트 - 5분 경과 시 취소 실패")
    void cancelOrder_Fail_TimeExceeded() {
        // given
        UUID orderId = UUID.randomUUID();
        String userId = "user01";
        Order order = mock(Order.class);

        given(orderRepository.findByIdAndDeletedAtIsNull(orderId)).willReturn(Optional.of(order));
        given(order.getUserId()).willReturn(userId);
        given(order.getCreatedAt()).willReturn(LocalDateTime.now().minusMinutes(6)); // 6분 전 생성

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> 
            orderService.cancelOrder(orderId, userId, "CUSTOMER")
        );
        assertEquals("주문 생성 후 5분이 경과하여 취소할 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("주문 상세 조회 테스트 - 권한 없는 사용자가 조회 시 실패")
    void getOrder_Fail_Unauthorized() {
        // given
        UUID orderId = UUID.randomUUID();
        Order order = mock(Order.class);
        given(orderRepository.findByIdAndDeletedAtIsNull(orderId)).willReturn(Optional.of(order));
        given(order.getUserId()).willReturn("owner_user");

        // when & then
        assertThrows(IllegalStateException.class, () -> 
            orderService.getOrder(orderId, "other_user", "CUSTOMER")
        );
    }
}
