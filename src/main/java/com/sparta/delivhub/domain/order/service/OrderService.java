package com.sparta.delivhub.domain.order.service;

import com.sparta.delivhub.domain.order.dto.OrderRequestDto;
import com.sparta.delivhub.domain.order.dto.OrderResponseDto;
import com.sparta.delivhub.domain.order.entity.Order;
import com.sparta.delivhub.domain.order.entity.OrderItem;
import com.sparta.delivhub.domain.order.entity.OrderStatus;
import com.sparta.delivhub.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

    /**
     * 주문 생성
     */
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto requestDto, String userId) {
        // 1. totalPrice 계산을 위한 준비
        long totalPrice = 0;
        
        // 2. 임시 리스트에 OrderItem 저장
        java.util.List<OrderItem> orderItems = new java.util.ArrayList<>();
        for (OrderRequestDto.OrderItemRequestDto itemDto : requestDto.getItems()) {
            long unitPrice = getMenuPrice(itemDto.getMenuId());
            OrderItem orderItem = OrderItem.builder()
                    .menuId(itemDto.getMenuId())
                    .quantity(itemDto.getQuantity())
                    .unitPrice(unitPrice)
                    .build();
            orderItems.add(orderItem);
            totalPrice += unitPrice * itemDto.getQuantity();
        }

        // 3. 최종 Order 생성
        Order order = Order.builder()
                .userId(userId)
                .storeId(requestDto.getStoreId())
                .addressId(requestDto.getAddressId())
                .request(requestDto.getRequest())
                .orderType(requestDto.getOrderType())
                .totalPrice(totalPrice)
                .build();

        // 4. 연관 관계 설정
        orderItems.forEach(order::addOrderItem);

        return OrderResponseDto.from(orderRepository.save(order));
    }

    /**
     * 전체 주문 조회 및 검색 (권한별 필터링)
     */
    public Page<OrderResponseDto> getOrders(String userId, String role, UUID storeId, OrderStatus status, int page, int size) {
        // size 제한 (10, 30, 50)
        if (size != 10 && size != 30 && size != 50) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orderPage;

        // 역할별 조회 분기
        if (role.equals("MASTER") || role.equals("MANAGER")) {
            orderPage = orderRepository.findAllByDeletedAtIsNull(pageable);
        } else if (role.equals("OWNER")) {
            // 실제 구현 시 Owner가 소유한 Store ID 목록을 가져와야 함
            orderPage = orderRepository.findAllByDeletedAtIsNull(pageable); 
        } else {
            orderPage = orderRepository.findAllByUserIdAndDeletedAtIsNull(userId, pageable);
        }

        return orderPage.map(OrderResponseDto::from);
    }

    /**
     * 주문 상세 조회
     */
    public OrderResponseDto getOrder(UUID orderId, String userId, String role) {
        Order order = orderRepository.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        // 권한 체크 (본인 주문이거나 관리자여야 함)
        if (!role.equals("MASTER") && !role.equals("MANAGER") && !order.getUserId().equals(userId)) {
            throw new IllegalStateException("조회 권한이 없습니다.");
        }

        return OrderResponseDto.from(order);
    }

    /**
     * 주문 요청사항 수정
     */
    @Transactional
    public OrderResponseDto updateRequest(UUID orderId, String newRequest, String userId) {
        Order order = findOrderAndCheckOwnership(orderId, userId);
        order.updateRequest(newRequest);
        return OrderResponseDto.from(order);
    }

    /**
     * 주문 상태 변경
     */
    @Transactional
    public OrderResponseDto updateStatus(UUID orderId, OrderStatus nextStatus, String userId, String role) {
        Order order = orderRepository.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        // OWNER 권한 체크: 본인 가게 주문인지 확인 로직 필요
        order.updateStatus(nextStatus);
        return OrderResponseDto.from(order);
    }

    /**
     * 주문 취소 (5분 이내 제한)
     */
    @Transactional
    public OrderResponseDto cancelOrder(UUID orderId, String userId, String role) {
        Order order = findOrderAndCheckOwnership(orderId, userId);

        // 시간 검증: 5분(300초) 이내인지 확인
        long minutesPassed = Duration.between(order.getCreatedAt(), LocalDateTime.now()).toMinutes();
        if (minutesPassed >= 5 && !role.equals("MASTER")) {
            throw new IllegalStateException("주문 생성 후 5분이 경과하여 취소할 수 없습니다.");
        }

        order.cancel();
        return OrderResponseDto.from(order);
    }

    /**
     * 주문 내역 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteOrder(UUID orderId, String adminId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        order.softDelete(adminId);
    }

    private Order findOrderAndCheckOwnership(UUID orderId, String userId) {
        Order order = orderRepository.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        if (!order.getUserId().equals(userId)) {
            throw new IllegalStateException("권한이 없습니다.");
        }
        return order;
    }

    private long getMenuPrice(UUID menuId) {
        // 실제 MenuRepository 조회 로직이 들어갈 자리
        return 10000L; 
    }
}
