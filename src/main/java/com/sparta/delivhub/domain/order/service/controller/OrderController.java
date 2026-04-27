package com.sparta.delivhub.domain.order.service.controller;

import com.sparta.delivhub.common.dto.ApiResponse;
import com.sparta.delivhub.domain.order.service.dto.OrderRequestDto;
import com.sparta.delivhub.domain.order.service.dto.OrderResponseDto;
import com.sparta.delivhub.domain.order.service.entity.OrderStatus;
import com.sparta.delivhub.domain.order.service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<OrderResponseDto> createOrder(
            @Valid @RequestBody OrderRequestDto requestDto) {
        String userId = getCurrentUserId();
        return ApiResponse.success(orderService.createOrder(requestDto, userId));
    }

    @GetMapping
    public ApiResponse<Page<OrderResponseDto>> getOrders(
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String userId = getCurrentUserId();
        String role = getCurrentUserRole();
        return ApiResponse.success(orderService.getOrders(userId, role, storeId, status, page, size));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponseDto> getOrder(@PathVariable UUID orderId) {
        String userId = getCurrentUserId();
        String role = getCurrentUserRole();
        return ApiResponse.success(orderService.getOrder(orderId, userId, role));
    }

    @PutMapping("/{orderId}")
    public ApiResponse<OrderResponseDto> updateRequest(
            @PathVariable UUID orderId,
            @RequestBody String request) {
        String userId = getCurrentUserId();
        return ApiResponse.success(orderService.updateRequest(orderId, request, userId));
    }

    @PatchMapping("/{orderId}/status")
    public ApiResponse<OrderResponseDto> updateStatus(
            @PathVariable UUID orderId,
            @RequestBody OrderStatus status) {
        String userId = getCurrentUserId();
        String role = getCurrentUserRole();
        return ApiResponse.success(orderService.updateStatus(orderId, status, userId, role));
    }

    @PatchMapping("/{orderId}/cancel")
    public ApiResponse<OrderResponseDto> cancelOrder(@PathVariable UUID orderId) {
        String userId = getCurrentUserId();
        String role = getCurrentUserRole();
        return ApiResponse.success(orderService.cancelOrder(orderId, userId, role));
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteOrder(@PathVariable UUID orderId) {
        String adminId = getCurrentUserId();
        orderService.deleteOrder(orderId, adminId);
        return ApiResponse.success(null);
    }

    /**
     * 보안 강화: HTTP 헤더 직접 추출 대신 SecurityContext에서 사용자 ID 획득
     */
    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "anonymous";
    }

    private String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && !auth.getAuthorities().isEmpty() 
            ? auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "") 
            : "GUEST";
    }
}
