package com.sparta.delivhub.domain.store.controller;

import com.sparta.delivhub.common.dto.ApiResponse;
import com.sparta.delivhub.domain.payment.dto.StorePaymentListResponseDto;
import com.sparta.delivhub.domain.payment.service.PaymentService;
import com.sparta.delivhub.domain.review.service.ReviewService;
import com.sparta.delivhub.domain.store.dto.requset.StoreRequestDto;
import com.sparta.delivhub.domain.store.dto.response.StoreDetailResponseDto;
import com.sparta.delivhub.domain.store.dto.response.StoreIdResponseDto;
import com.sparta.delivhub.domain.store.dto.response.StoreListResponseDto;
import com.sparta.delivhub.domain.review.dto.StoreReviewPageResponseDto;
import com.sparta.delivhub.domain.store.service.StoreService;
import com.sparta.delivhub.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StoreController {
    private final StoreService storeService;
    private final ReviewService reviewService;
    private final PaymentService paymentService;

    @PostMapping("/stores")
    public ApiResponse<StoreIdResponseDto> createStore(@RequestBody StoreRequestDto storeRequestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        StoreIdResponseDto id = storeService.createStore(storeRequestDto, userDetails.getUsername());
        return ApiResponse.success(id);
    }

    @GetMapping("/stores")
    public ApiResponse<List<StoreListResponseDto>> getAllStores(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "createdAt,DESC") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return ApiResponse.success(storeService.findAllStores(pageable));
    }

    @GetMapping("/stores/{storeId}")
    public ApiResponse<StoreDetailResponseDto> getStore(@PathVariable("storeId") UUID storeId) {
        return ApiResponse.success(storeService.findStore(storeId));
    }

    @PutMapping("/stores/{storeId}")
    public ApiResponse<StoreIdResponseDto> updateStore(@PathVariable("storeId") UUID storeId, @RequestBody StoreRequestDto storeRequestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        StoreIdResponseDto id = storeService.updateStore(storeId, storeRequestDto, userDetails.getUsername());
        return ApiResponse.success(id);
    }

    @DeleteMapping("/stores/{storeId}")
    public ApiResponse<StoreIdResponseDto> deleteStore(@PathVariable("storeId") UUID storeId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        StoreIdResponseDto id = storeService.deleteStore(storeId, userDetails.getUsername());
        return ApiResponse.success(id);
    }

    /**
     * 특정 가게별 리뷰 조회 (권한: 전체)
     */
    @GetMapping("/stores/{storeId}/reviews")
    public ResponseEntity<ApiResponse<StoreReviewPageResponseDto>> getReviewsByStore(
            @PathVariable UUID storeId,
            @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {

        StoreReviewPageResponseDto responseData = reviewService.getReviewsByStore(storeId, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responseData));
    }

    /**
     * 가게별 결제 목록 조회 (권한: OWNER, MANAGER, MASTER)
     */
    @GetMapping("/stores/{storeId}/payments")
    public ResponseEntity<ApiResponse<StorePaymentListResponseDto>> getStorePayments(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        // 1. 유저 정보 및 권한 추출
        String currentUserId = userDetails.getUsername();

        // 2. 서비스 로직 호출 (결제 내역 조회이므로 PaymentService 사용)
        StorePaymentListResponseDto responseData = paymentService.getStorePayments(storeId, currentUserId, pageable);

        // 3. 성공 응답 반환 (200 OK)
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responseData));
    }

}
