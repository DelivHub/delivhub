package com.sparta.delivhub.domain.user.controller;

import com.sparta.delivhub.common.dto.ApiResponse;
import com.sparta.delivhub.domain.payment.dto.MyPaymentListResponseDto;
import com.sparta.delivhub.domain.payment.service.PaymentService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    private final PaymentService paymentService;

    public UserController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 내 결제 내역 전체 조회
     */
    @GetMapping("/api/v1/users/payments")
    public ResponseEntity<ApiResponse<MyPaymentListResponseDto>> getMyPayments(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        // 1. 시큐리티 토큰에서 로그인한 유저 정보 추출
        String currentUserId = userDetails.getUsername();

        // 2. 서비스 로직 호출
        MyPaymentListResponseDto responseData = paymentService.getMyPayments(currentUserId, pageable);

        // 3. 성공 응답 반환 (200 OK)
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responseData));
    }
}
