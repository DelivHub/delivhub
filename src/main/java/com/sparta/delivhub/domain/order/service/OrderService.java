package com.sparta.delivhub.domain.order.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.domain.menu.entity.Menu;
import com.sparta.delivhub.domain.menu.repository.MenuRepository;
import com.sparta.delivhub.domain.option.entity.OptionItem;
import com.sparta.delivhub.domain.option.entity.OptionType;
import com.sparta.delivhub.domain.option.repository.OptionItemRepository;
import com.sparta.delivhub.domain.order.dto.OrderRequestDto;
import com.sparta.delivhub.domain.order.dto.OrderResponseDto;
import com.sparta.delivhub.domain.order.entity.Order;
import com.sparta.delivhub.domain.order.entity.OrderItem;
import com.sparta.delivhub.domain.order.entity.OrderItemOption;
import com.sparta.delivhub.domain.order.entity.OrderStatus;
import com.sparta.delivhub.domain.order.exception.OrderCancellationNotAllowedException;
import com.sparta.delivhub.domain.order.exception.OrderNotFoundException;
import com.sparta.delivhub.domain.order.exception.UnauthorizedOrderAccessException;
import com.sparta.delivhub.domain.order.repository.OrderRepository;
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
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private static final int ORDER_CANCEL_LIMIT_MINUTES = 5;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final MenuRepository menuRepository;
    private final OptionItemRepository optionItemRepository;

    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto requestDto, String userId) {
        long totalPrice = 0;
        List<OrderItem> orderItems = new java.util.ArrayList<>();

        for (OrderRequestDto.OrderItemRequestDto itemDto : requestDto.getItems()) {
            Menu menu = menuRepository.findByIdAndDeletedAtIsNull(itemDto.getMenuId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND_ON_READ));

            validateMenuBelongsToStore(menu, requestDto.getStoreId());
            List<OptionItem> optionItems = getOptionItems(itemDto.getOptionItemIds());
            validateOptionItemsBelongToMenu(optionItems, itemDto.getMenuId());
            validateSingleOptionSelection(optionItems);

            long optionExtraPrice = optionItems.stream()
                    .mapToLong(OptionItem::getExtraPrice)
                    .sum();

            long unitPrice = menu.getPrice().longValue() + optionExtraPrice;

            OrderItem orderItem = OrderItem.builder()
                    .menuId(itemDto.getMenuId())
                    .quantity(itemDto.getQuantity())
                    .unitPrice(unitPrice)
                    .build();
            for (OptionItem optionItem : optionItems) {
                OrderItemOption orderItemOption = OrderItemOption.builder()
                        .optionId(optionItem.getOption().getId())
                        .optionName(optionItem.getOption().getName())
                        .optionItemId(optionItem.getId())
                        .optionItemName(optionItem.getName())
                        .extraPrice(optionItem.getExtraPrice())
                        .build();

                orderItem.addOption(orderItemOption);
            }
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

    private List<OptionItem> getOptionItems(List<UUID> optionItemIds) {
        if (optionItemIds == null || optionItemIds.isEmpty()) {
            return List.of();
        }

        List<OptionItem> optionItems = optionItemRepository.findByIdInAndDeletedAtIsNull(optionItemIds);

        if (optionItems.size() != optionItemIds.size()) {
            throw new BusinessException(ErrorCode.OPTION_ITEM_NOT_FOUND);
        }

        Map<UUID, OptionItem> optionItemMap = optionItems.stream()
                .collect(Collectors.toMap(OptionItem::getId, Function.identity()));

        return optionItemIds.stream()
                .map(optionItemMap::get)
                .toList();
    }

    private void validateMenuBelongsToStore(Menu menu, UUID storeId) {
        if (!menu.getStore().getId().equals(storeId)) {
            throw new BusinessException(ErrorCode.MENU_NOT_FOUND_ON_READ);
        }
    }

    private void validateOptionItemsBelongToMenu(List<OptionItem> optionItems, UUID menuId) {
        boolean hasInvalidOptionItem = optionItems.stream()
                .anyMatch(optionItem -> !optionItem.getOption().getMenu().getId().equals(menuId));

        if (hasInvalidOptionItem) {
            throw new BusinessException(ErrorCode.OPTION_ITEM_NOT_FOUND);
        }
    }

    private void validateSingleOptionSelection(List<OptionItem> optionItems) {
        Map<UUID, Long> selectedCountByOptionGroup = optionItems.stream()
                .filter(optionItem -> optionItem.getOption().getType() == OptionType.SINGLE)
                .collect(Collectors.groupingBy(
                        optionItem -> optionItem.getOption().getId(),
                        Collectors.counting()
                ));

        boolean hasDuplicatedSingleOption = selectedCountByOptionGroup.values().stream()
                .anyMatch(count -> count > 1);

        if (hasDuplicatedSingleOption) {
            throw new BusinessException(ErrorCode.OPTION_ITEM_VALIDATION_ERROR);
        }
    }

    private int validatePageSize(int size) {
        if (size == 10 || size == 30 || size == 50) {
            return size;
        }
        return 10;
    }

    private List<UUID> getOwnerStoreIds(String userId) {
        return List.of(); 
    }
}
