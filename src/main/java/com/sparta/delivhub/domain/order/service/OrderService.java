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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto requestDto, String userId) {
        long totalPrice = 0;
        List<OrderItem> orderItems = new java.util.ArrayList<>();

        for (OrderRequestDto.OrderItemRequestDto itemDto : requestDto.getItems()) {
            long unitPrice = getActualMenuPrice(itemDto.getMenuId());
            OrderItem orderItem = OrderItem.builder()
                    .menuId(itemDto.getMenuId())
                    .quantity(itemDto.getQuantity())
                    .unitPrice(unitPrice)
                    .build();
            orderItems.add(orderItem);
            totalPrice += unitPrice * itemDto.getQuantity();
        }

        Order order = Order.builder()
                .userId(userId)
                .storeId(requestDto.getStoreId())
                .addressId(requestDto.getAddressId())
                .request(requestDto.getRequest())
                .orderType(requestDto.getOrderType())
                .totalPrice(totalPrice)
                .build();

        orderItems.forEach(order::addOrderItem);

        return OrderResponseDto.from(orderRepository.save(order));
    }

    public Page<OrderResponseDto> getOrders(String userId, String role, UUID storeId, OrderStatus status, int page, int size) {
        if (size != 10 && size != 30 && size != 50) size = 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (role.equals("MASTER") || role.equals("MANAGER")) {
            return orderRepository.findAllByDeletedAtIsNull(pageable).map(OrderResponseDto::from);
        }

        if (role.equals("OWNER")) {
            List<UUID> ownerStoreIds = getOwnerStoreIds(userId);
            return orderRepository.findAllByStoreIdInAndDeletedAtIsNull(ownerStoreIds, pageable).map(OrderResponseDto::from);
        }

        return orderRepository.findAllByUserIdAndDeletedAtIsNull(userId, pageable).map(OrderResponseDto::from);
    }

    public OrderResponseDto getOrder(UUID orderId, String userId, String role) {
        Order order = orderRepository.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        if (!role.equals("MASTER") && !role.equals("MANAGER") && !order.getUserId().equals(userId)) {
            throw new IllegalStateException("조회 권한이 없습니다.");
        }

        return OrderResponseDto.from(order);
    }

    @Transactional
    public OrderResponseDto updateRequest(UUID orderId, String newRequest, String userId) {
        Order order = orderRepository.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        if (!order.getUserId().equals(userId)) {
            throw new IllegalStateException("본인 주문만 수정 가능합니다.");
        }
        order.updateRequest(newRequest);
        return OrderResponseDto.from(order);
    }

    @Transactional
    public OrderResponseDto updateStatus(UUID orderId, OrderStatus nextStatus, String userId, String role) {
        Order order = orderRepository.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        if (role.equals("OWNER")) {
            List<UUID> ownerStoreIds = getOwnerStoreIds(userId);
            if (!ownerStoreIds.contains(order.getStoreId())) {
                throw new IllegalStateException("본인 가게의 주문만 상태를 변경할 수 있습니다.");
            }
        }

        order.updateStatus(nextStatus);
        return OrderResponseDto.from(order);
    }

    @Transactional
    public OrderResponseDto cancelOrder(UUID orderId, String userId, String role) {
        Order order = orderRepository.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        if (!role.equals("MASTER") && !order.getUserId().equals(userId)) {
            throw new IllegalStateException("권한이 없습니다.");
        }

        long minutesPassed = Duration.between(order.getCreatedAt(), LocalDateTime.now()).toMinutes();
        if (minutesPassed >= 5 && !role.equals("MASTER")) {
            throw new IllegalStateException("주문 생성 후 5분이 경과하여 취소할 수 없습니다.");
        }

        order.cancel();
        return OrderResponseDto.from(order);
    }

    @Transactional
    public void deleteOrder(UUID orderId, String adminId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        order.softDelete(adminId);
    }

    private long getActualMenuPrice(UUID menuId) {
        return 15000L;
    }

    private List<UUID> getOwnerStoreIds(String userId) {
        return List.of(); 
    }
}
