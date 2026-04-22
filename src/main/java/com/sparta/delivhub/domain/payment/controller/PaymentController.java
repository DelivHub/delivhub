package com.sparta.delivhub.domain.payment.controller;

import com.sparta.delivhub.common.dto.ApiResponse;
import com.sparta.delivhub.domain.payment.dto.RequestPaymentDTO;
import com.sparta.delivhub.domain.payment.dto.ResponsePaymentDTO;
import com.sparta.delivhub.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 결제 내역 생성
    @PostMapping
    public ResponseEntity<ApiResponse<ResponsePaymentDTO>> createPayment(
            @Valid @RequestBody RequestPaymentDTO request,
            @RequestHeader("X-User-Id") String currentUserId) {

        // 1. 서비스 로직 실행
        ResponsePaymentDTO responseData = paymentService.createPayment(request, currentUserId);

        // 2. API 명세서의 공통 응답 규격 맞추기
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(responseData));
    }

    // 결제 상세 조회 paymentId 로 조회
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<ResponsePaymentDTO>> getPayment(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String currentUserId = userDetails.getUsername();
        String userRole = userDetails.getAuthorities().iterator().next().getAuthority();

        ResponsePaymentDTO responseData = paymentService.getPayment(paymentId, currentUserId, userRole);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responseData));
    }
    
    // 결제 내역 삭제 (소프트)
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<Void>> deletePayment(
            @PathVariable UUID paymentId,
            @RequestHeader("X-User-Id") String currentUserId) {

        paymentService.deletePayment(paymentId, currentUserId);

        // 데이터가 없는 200 OK 응답
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success());
    }
}