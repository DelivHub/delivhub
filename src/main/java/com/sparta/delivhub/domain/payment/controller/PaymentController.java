package com.sparta.delivhub.domain.payment.controller;

import com.sparta.delivhub.common.dto.ApiResponse;
import com.sparta.delivhub.domain.payment.dto.RequestPaymentDTO;
import com.sparta.delivhub.domain.payment.dto.ResponsePaymentDTO;
import com.sparta.delivhub.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 결제 내역 생성
    @PostMapping
    public ResponseEntity<ApiResponse<ResponsePaymentDTO>> createPayment(
            @Valid @RequestBody RequestPaymentDTO request, // @Valid가 DTO의 제약조건을 검사합니다.
            @RequestHeader("X-User-Id") String currentUserId) {

        // 1. 서비스 로직 실행
        ResponsePaymentDTO responseData = paymentService.createPayment(request, currentUserId);

        // 2. API 명세서의 공통 응답 규격 맞추기
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(responseData));
    }
    
    
    // 결제 내역 삭제
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<Void>> deletePayment(
            @PathVariable UUID paymentId,
            @RequestHeader("X-User-Id") String currentUserId) {

        paymentService.deletePayment(paymentId, currentUserId);

        // 데이터가 없는 200 OK 응답
        return ResponseEntity.ok(ApiResponse.success());
    }
}