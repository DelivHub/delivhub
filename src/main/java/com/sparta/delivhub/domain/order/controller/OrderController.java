package com.sparta.delivhub.domain.order.controller;

import com.sparta.delivhub.common.dto.ApiResponse;
import com.sparta.delivhub.domain.order.dto.OrderRequestDto;
import com.sparta.delivhub.domain.order.dto.OrderResponseDto;
import com.sparta.delivhub.domain.order.entity.OrderStatus;
import com.sparta.delivhub.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<OrderResponseDto> createOrder(
            @Valid @RequestBody OrderRequestDto requestDto,
            @RequestHeader("X-User-Id") String userId) { // 시연용: 실제로는 SecurityContext에서 추출
        return ApiResponse.success(orderService.createOrder(requestDto, userId));
    }

    @GetMapping
    public ApiResponse<Page<OrderResponseDto>> getOrders(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(orderService.getOrders(userId, role, storeId, status, page, size));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponseDto> getOrder(
            @PathVariable UUID orderId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {
        return ApiResponse.success(orderService.getOrder(orderId, userId, role));
    }

    @PutMapping("/{orderId}")
    public ApiResponse<OrderResponseDto> updateRequest(
            @PathVariable UUID orderId,
            @RequestBody String request,
            @RequestHeader("X-User-Id") String userId) {
        return ApiResponse.success(orderService.updateRequest(orderId, request, userId));
    }

    @PatchMapping("/{orderId}/status")
    public ApiResponse<OrderResponseDto> updateStatus(
            @PathVariable UUID orderId,
            @RequestBody OrderStatus status,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {
        return ApiResponse.success(orderService.updateStatus(orderId, status, userId, role));
    }

    @PatchMapping("/{orderId}/cancel")
    public ApiResponse<OrderResponseDto> cancelOrder(
            @PathVariable UUID orderId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {
        return ApiResponse.success(orderService.cancelOrder(orderId, userId, role));
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteOrder(
            @PathVariable UUID orderId,
            @RequestHeader("X-User-Id") String adminId) {
        orderService.deleteOrder(orderId, adminId);
        return ApiResponse.success(null);
    }
}
