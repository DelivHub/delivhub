package com.sparta.delivhub.domain.payment.controller;

import com.sparta.delivhub.common.dto.ApiResponse;
import com.sparta.delivhub.domain.payment.dto.*;
import com.sparta.delivhub.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    /**
     * 결제 내역 생성 (user)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ResponsePaymentDTO>> createPayment(
            @Valid @RequestBody RequestPaymentDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String currentUserId = userDetails.getUsername();

        // 1. 서비스 로직 실행
        ResponsePaymentDTO responseData = paymentService.createPayment(request, currentUserId);

        // 2. API 명세서의 공통 응답 규격 맞추기
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(responseData));
    }
    /**
     * 결제 상세 조회 (paymentId 로 조회)
     */
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

    /**
     * 결제 상태 수정 (관리자 전용)
     */
    @PatchMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<ResponsePaymentDTO>> updatePaymentStatus(
            @PathVariable UUID paymentId, // URL에서 어떤 결제건인지 식별
            @Valid @RequestBody RequestUpdatePaymentStatusDTO request, // Body에서 바꿀 상태값을 받음
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // 1. 유저 권한 추출
        String userRole = userDetails.getAuthorities().iterator().next().getAuthority();

        // 2. 서비스 로직 실행 (상태값 String을 함께 넘겨줍니다)
        ResponsePaymentDTO responseData = paymentService.updatePaymentStatus(
                paymentId,
                request.getStatus(),
                userRole
        );

        // 3. 공통 응답 규격 반환 (200 OK)
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responseData));
    }
    /**
     * 결제 내역 삭제 (소프트)
     */
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<Void>> deletePayment(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String currentUserId = userDetails.getUsername();

        paymentService.deletePayment(paymentId, currentUserId);

        // 데이터가 없는 200 OK 응답
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success());
    }
    /**
     * 가게별 결제 목록 조회 (권한: OWNER, MANAGER, MASTER)
     */
    @GetMapping("/api/v1/stores/{storeId}/payments")
    public ResponseEntity<ApiResponse<StorePaymentListResponseDto>> getStorePayments(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        // 1. 유저 정보 및 권한 추출
        String currentUserId = userDetails.getUsername();
        String userRole = userDetails.getAuthorities().iterator().next().getAuthority();

        // 2. 서비스 로직 호출 (결제 내역 조회이므로 PaymentService 사용)
        StorePaymentListResponseDto responseData = paymentService.getStorePayments(storeId, currentUserId, userRole, pageable);

        // 3. 성공 응답 반환 (200 OK)
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responseData));
    }

}