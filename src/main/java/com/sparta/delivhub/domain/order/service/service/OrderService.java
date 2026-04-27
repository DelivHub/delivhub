package com.sparta.delivhub.domain.order.service.service;

import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.domain.order.service.dto.OrderRequestDto;
import com.sparta.delivhub.domain.order.service.dto.OrderResponseDto;
import com.sparta.delivhub.domain.order.service.entity.Order;
import com.sparta.delivhub.domain.order.service.entity.OrderItem;
import com.sparta.delivhub.domain.order.service.entity.OrderStatus;
import com.sparta.delivhub.domain.order.service.exception.OrderCancellationNotAllowedException;
import com.sparta.delivhub.domain.order.service.exception.OrderNotFoundException;
import com.sparta.delivhub.domain.order.service.exception.UnauthorizedOrderAccessException;
import com.sparta.delivhub.domain.order.service.repository.OrderRepository;
import com.sparta.delivhub.domain.payment.entity.PaymentStatus;
import com.sparta.delivhub.domain.payment.repository.PaymentRepository;
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

    private static final int ORDER_CANCEL_LIMIT_MINUTES = 5;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

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
        int validatedSize = validatePageSize(size);
        Pageable pageable = PageRequest.of(page, validatedSize, Sort.by("createdAt").descending());

        if (role.equals("MASTER") || role.equals("MANAGER")) {
            return orderRepository.findAll(pageable).map(OrderResponseDto::from);
        }

        if (role.equals("OWNER")) {
            List<UUID> ownerStoreIds = getOwnerStoreIds(userId);
            return orderRepository.findAllByStoreIdIn(ownerStoreIds, pageable).map(OrderResponseDto::from);
        }

        return orderRepository.findAllByUserId(userId, pageable).map(OrderResponseDto::from);
    }

    public OrderResponseDto getOrder(UUID orderId, String userId, String role) {
        Order order = findOrderOrThrow(orderId);

        if (!role.equals("MASTER") && !role.equals("MANAGER") && !order.getUserId().equals(userId)) {
            throw new UnauthorizedOrderAccessException(ErrorCode.ORDER_READ_FORBIDDEN);
        }

        return OrderResponseDto.from(order);
    }

    @Transactional
    public OrderResponseDto updateRequest(UUID orderId, String newRequest, String userId) {
        Order order = findOrderOrThrow(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedOrderAccessException(ErrorCode.ORDER_UPDATE_FORBIDDEN);
        }
        order.updateRequest(newRequest);
        return OrderResponseDto.from(order);
    }

    @Transactional
    public OrderResponseDto updateStatus(UUID orderId, OrderStatus nextStatus, String userId, String role) {
        Order order = findOrderOrThrow(orderId);

        if (role.equals("OWNER")) {
            List<UUID> ownerStoreIds = getOwnerStoreIds(userId);
            if (!ownerStoreIds.contains(order.getStoreId())) {
                throw new UnauthorizedOrderAccessException(ErrorCode.ORDER_STATUS_UPDATE_FORBIDDEN);
            }
        } else if (!role.equals("MASTER") && !role.equals("MANAGER")) {
            throw new UnauthorizedOrderAccessException(ErrorCode.ORDER_STATUS_UPDATE_FORBIDDEN);
        }

        order.updateStatus(nextStatus);
        return OrderResponseDto.from(order);
    }

    @Transactional
    public OrderResponseDto cancelOrder(UUID orderId, String userId, String role) {
        Order order = findOrderOrThrow(orderId);

        if (!role.equals("MASTER") && !order.getUserId().equals(userId)) {
            throw new UnauthorizedOrderAccessException();
        }

        long minutesPassed = Duration.between(order.getCreatedAt(), LocalDateTime.now()).toMinutes();
        if (minutesPassed >= ORDER_CANCEL_LIMIT_MINUTES && !role.equals("MASTER")) {
            throw new OrderCancellationNotAllowedException();
        }

        order.cancel();
        // [추가된 로직] 연관된 결제 내역을 찾아 결제 상태도 CANCELED로 변경
        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
            // Payment 엔티티에 있는 상태 변경 메서드 호출
            payment.updateStatus(PaymentStatus.CANCELLED);
        });

        return OrderResponseDto.from(order);
    }

    @Transactional
    public void deleteOrder(UUID orderId, String adminId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);
        order.softDelete(adminId);
    }

    private Order findOrderOrThrow(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);
    }

    private int validatePageSize(int size) {
        if (size == 10 || size == 30 || size == 50) {
            return size;
        }
        return 10;
    }

    private long getActualMenuPrice(UUID menuId) {
        return 15000L;
    }

    private List<UUID> getOwnerStoreIds(String userId) {
        return List.of(); 
    }
}
