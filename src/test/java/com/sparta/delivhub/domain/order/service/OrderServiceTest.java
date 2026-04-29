package com.sparta.delivhub.domain.order.service;

import com.sparta.delivhub.domain.menu.entity.Menu;
import com.sparta.delivhub.domain.menu.repository.MenuRepository;
import com.sparta.delivhub.domain.option.repository.OptionItemRepository;
import com.sparta.delivhub.domain.order.dto.OrderRequestDto;
import com.sparta.delivhub.domain.order.dto.OrderResponseDto;
import com.sparta.delivhub.domain.order.entity.Order;
import com.sparta.delivhub.domain.order.entity.OrderStatus;
import com.sparta.delivhub.domain.order.entity.OrderType;
import com.sparta.delivhub.domain.order.exception.OrderCancellationNotAllowedException;
import com.sparta.delivhub.domain.order.exception.UnauthorizedOrderAccessException;
import com.sparta.delivhub.domain.order.repository.OrderRepository;
import com.sparta.delivhub.domain.payment.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private OptionItemRepository optionItemRepository;

    @Test
    @DisplayName("주문 생성 테스트 - 성공")
    void createOrder_Success() {
        // given
        String userId = "user01";
        UUID menuId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID optionItemId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();

        OrderRequestDto requestDto = OrderRequestDto.builder()
                .storeId(storeId)
                .addressId(UUID.randomUUID())
                .request("많이 주세요")
                .orderType(OrderType.ONLINE)
                .items(List.of(
                        OrderRequestDto.OrderItemRequestDto.builder()
                                .menuId(menuId)
                                .quantity(2)
                                .optionItemIds(List.of(optionItemId))
                                .build()
                ))
                .build();

        // Store mock
        com.sparta.delivhub.domain.store.entity.Store mockStore = mock(com.sparta.delivhub.domain.store.entity.Store.class);
        when(mockStore.getId()).thenReturn(storeId);

        // Menu mock
        Menu mockMenu = mock(Menu.class);
        when(mockMenu.getStore()).thenReturn(mockStore);
        when(mockMenu.getPrice()).thenReturn(15000);
        when(mockMenu.getId()).thenReturn(menuId);

        // Option mock (SINGLE 타입)
        com.sparta.delivhub.domain.option.entity.Option mockOption = mock(com.sparta.delivhub.domain.option.entity.Option.class);
        when(mockOption.getId()).thenReturn(optionId);
        when(mockOption.getName()).thenReturn("맵기 선택");
        when(mockOption.getType()).thenReturn(com.sparta.delivhub.domain.option.entity.OptionType.SINGLE);
        when(mockOption.getMenu()).thenReturn(mockMenu);

        // OptionItem mock
        com.sparta.delivhub.domain.option.entity.OptionItem mockOptionItem = mock(com.sparta.delivhub.domain.option.entity.OptionItem.class);
        when(mockOptionItem.getId()).thenReturn(optionItemId);
        when(mockOptionItem.getName()).thenReturn("보통");
        when(mockOptionItem.getExtraPrice()).thenReturn(0L);
        when(mockOptionItem.getOption()).thenReturn(mockOption);

        when(menuRepository.findByIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.of(mockMenu));
        when(optionItemRepository.findByIdInAndDeletedAtIsNull(List.of(optionItemId))).thenReturn(List.of(mockOptionItem));

        Order order = Order.builder()
                .userId(userId)
                .storeId(storeId)
                .addressId(requestDto.getAddressId())
                .request(requestDto.getRequest())
                .orderType(requestDto.getOrderType())
                .totalPrice(30000L)
                .build();
        ReflectionTestUtils.setField(order, "id", UUID.randomUUID());

        given(orderRepository.save(any(Order.class))).willReturn(order);

        // when
        OrderResponseDto response = orderService.createOrder(requestDto, userId);

        // then
        assertNotNull(response);
        assertEquals(userId, order.getUserId());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("주문 목록 조회 테스트 - MASTER 권한은 모든 주문 조회")
    void getOrders_Master_Success() {
        // given
        Order order = Order.builder().userId("user01").build();
        Page<Order> orderPage = new PageImpl<>(List.of(order));

        given(orderRepository.findAll(any(Pageable.class))).willReturn(orderPage);

        // when
        Page<OrderResponseDto> result = orderService.getOrders("master", "MASTER", null, null, 0, 10);

        // then
        assertEquals(1, result.getTotalElements());
        verify(orderRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("주문 목록 조회 테스트 - CUSTOMER 권한은 본인 주문만 조회")
    void getOrders_Customer_Success() {
        // given
        String userId = "user01";
        Order order = Order.builder().userId(userId).build();
        Page<Order> orderPage = new PageImpl<>(List.of(order));

        given(orderRepository.findAllByUserId(eq(userId), any(Pageable.class))).willReturn(orderPage);

        // when
        Page<OrderResponseDto> result = orderService.getOrders(userId, "CUSTOMER", null, null, 0, 10);

        // then
        assertEquals(1, result.getTotalElements());
        verify(orderRepository).findAllByUserId(eq(userId), any(Pageable.class));
    }

    @Test
    @DisplayName("주문 상세 조회 테스트 - 본인 주문 조회 성공")
    void getOrder_Success() {
        // given
        UUID orderId = UUID.randomUUID();
        String userId = "user01";
        Order order = Order.builder().userId(userId).build();
        ReflectionTestUtils.setField(order, "id", orderId);

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        // when
        OrderResponseDto response = orderService.getOrder(orderId, userId, "CUSTOMER");

        // then
        assertNotNull(response);
        assertEquals(orderId, response.getOrderId());
    }

    @Test
    @DisplayName("주문 상세 조회 테스트 - 권한 없는 사용자가 조회 시 실패")
    void getOrder_Fail_Unauthorized() {
        // given
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder().userId("owner_user").build();
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        // when & then
        assertThrows(UnauthorizedOrderAccessException.class, () ->
                orderService.getOrder(orderId, "other_user", "CUSTOMER")
        );
    }

    @Test
    @DisplayName("주문 취소 테스트 - 5분 이내 취소 성공")
    void cancelOrder_Success() {
        // given
        UUID orderId = UUID.randomUUID();
        String userId = "user01";
        Order order = spy(Order.builder().userId(userId).build());
        ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.now().minusMinutes(3));

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        // when
        orderService.cancelOrder(orderId, userId, "CUSTOMER");

        // then
        assertEquals(OrderStatus.CANCELED, order.getStatus());
    }

    @Test
    @DisplayName("주문 취소 테스트 - 5분 경과 시 취소 실패")
    void cancelOrder_Fail_TimeExceeded() {
        // given
        UUID orderId = UUID.randomUUID();
        String userId = "user01";
        Order order = Order.builder().userId(userId).build();
        ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.now().minusMinutes(6));

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        // when & then
        assertThrows(OrderCancellationNotAllowedException.class, () ->
                orderService.cancelOrder(orderId, userId, "CUSTOMER")
        );
    }

    @Test
    @DisplayName("페이지네이션 테스트 - 허용되지 않는 사이즈 입력 시 10으로 고정")
    void validatePageSize_Fallback() {
        // given
        given(orderRepository.findAll(any(Pageable.class))).willReturn(Page.empty());

        // when
        orderService.getOrders("master", "MASTER", null, null, 0, 25); // 25는 허용되지 않음

        // then
        verify(orderRepository).findAll(argThat((Pageable p) -> p.getPageSize() == 10));
    }
}
