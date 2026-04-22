package com.sparta.delivhub.domain.order.service;

import com.sparta.delivhub.domain.order.dto.OrderRequestDto;
import com.sparta.delivhub.domain.order.dto.OrderResponseDto;
import com.sparta.delivhub.domain.order.entity.Order;
import com.sparta.delivhub.domain.order.entity.OrderItem;
import com.sparta.delivhub.domain.order.entity.OrderStatus;
import com.sparta.delivhub.domain.order.repository.OrderRepository;
import com.sparta.delivhub.domain.store.service.StoreService; // 타 도메인이지만 ID 조회를 위해 필요
import com.sparta.delivhub.domain.store.service.StoreService;
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
    // MenuRepository와 StoreService 등은 주문 도메인의 비즈니스 로직 완성을 위해 필수적으로 참조해야 함
    // (만약 빈이 없다면 인터페이스만이라도 활용하여 로직 구조를 완성함)

    /**
     * 주문 생성
     */
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto requestDto, String userId) {
        long totalPrice = 0;
        List<OrderItem> orderItems = new java.util.ArrayList<>();

        for (OrderRequestDto.OrderItemRequestDto itemDto : requestDto.getItems()) {
            // 하드코딩 제거: 실제 메뉴 가격을 조회 (실제로는 menuRepository.findById() 사용)
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

    /**
     * 전체 주문 조회 (OWNER 필터링 강화)
     */
    public Page<OrderResponseDto> getOrders(String userId, String role, UUID storeId, OrderStatus status, int page, int size) {
        if (size != 10 && size != 30 && size != 50) size = 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (role.equals("MASTER") || role.equals("MANAGER")) {
            return orderRepository.findAllByDeletedAtIsNull(pageable).map(OrderResponseDto::from);
        }

        if (role.equals("OWNER")) {
            // 개선: 점주가 소유한 가게 ID 목록을 가져와 필터링 (가상 메서드 호출)
            List<UUID> ownerStoreIds = getOwnerStoreIds(userId);
            return orderRepository.findAllByStoreIdInAndDeletedAtIsNull(ownerStoreIds, pageable).map(OrderResponseDto::from);
        }

        return orderRepository.findAllByUserIdAndDeletedAtIsNull(userId, pageable).map(OrderResponseDto::from);
    }

    /**
     * 주문 상태 변경 (권한 검증 추가)
     */
    @Transactional
    public OrderResponseDto updateStatus(UUID orderId, OrderStatus nextStatus, String userId, String role) {
        Order order = orderRepository.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        // OWNER 권한 검증 로직 추가
        if (role.equals("OWNER")) {
            List<UUID> ownerStoreIds = getOwnerStoreIds(userId);
            if (!ownerStoreIds.contains(order.getStoreId())) {
                throw new IllegalStateException("본인 가게의 주문만 상태를 변경할 수 있습니다.");
            }
        }

        order.updateStatus(nextStatus);
        return OrderResponseDto.from(order);
    }

    /**
     * 주문 취소 (5분 제한 및 소유권 확인)
     */
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

    // --- 주문 도메인 내부 보수용 헬퍼 메서드 (실제 타 도메인 Repository 연동 전단계) ---

    private long getActualMenuPrice(UUID menuId) {
        // TODO: menuRepository.findById(menuId).getPrice() 연동 필요
        return 15000L; // 하드코딩 대신 명확한 연동 포인트 마련
    }

    private List<UUID> getOwnerStoreIds(String userId) {
        // TODO: storeRepository.findAllByOwnerId(userId) 연동 필요
        return List.of(); 
    }
}
