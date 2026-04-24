package com.sparta.delivhub.domain.review.controller;

import com.sparta.delivhub.common.dto.ApiResponse;
import com.sparta.delivhub.domain.review.dto.*;
import com.sparta.delivhub.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    
    /**
     * 리뷰 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponseDto>> createReview(
            @Valid @RequestBody ReviewRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // 1. 유저 정보 및 권한 추출
        String currentUserId = userDetails.getUsername();
        String userRole = userDetails.getAuthorities().iterator().next().getAuthority();

        // 2. 서비스 로직 호출
        ReviewResponseDto responseData = reviewService.createReview(request, currentUserId, userRole);

        // 3. 성공 응답 (201 CREATED 반환)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(responseData));
    }

    /**
     * 내 리뷰 조회 (페이징 적용)
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<MyReviewListResponseDto>> getMyReviews(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        // 1. 유저 정보 추출
        String currentUserId = userDetails.getUsername();
        String userRole = userDetails.getAuthorities().iterator().next().getAuthority();

        // 2. 서비스 로직 실행
        MyReviewListResponseDto responseData = reviewService.getMyReviews(currentUserId, userRole, pageable);

        // 3. 성공 응답 (200 OK)
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responseData));
    }

    /**
     * 모든 가게 리뷰 조회 (권한: 전체)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<StoreReviewListResponseDto>> getAllStoreReviews(
            @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        // 서비스 로직 실행
        StoreReviewListResponseDto responseData = reviewService.getAllStoreReviews(pageable);

        // 성공 응답 (200 OK)
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responseData));
    }

    /**
     * 리뷰 수정
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponseDto>> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewUpdateRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // 1. 유저 정보 추출
        String currentUserId = userDetails.getUsername();
        String userRole = userDetails.getAuthorities().iterator().next().getAuthority();

        // 2. 서비스 로직 호출
        ReviewResponseDto responseData = reviewService.updateReview(reviewId, request, currentUserId, userRole);

        // 3. 성공 응답 (200 OK 반환)
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responseData));
    }

    /**
     * 리뷰 삭제
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // 1. 유저 정보 추출
        String currentUserId = userDetails.getUsername();
        String userRole = userDetails.getAuthorities().iterator().next().getAuthority();

        // 2. 서비스 로직 호출
        reviewService.deleteReview(reviewId, currentUserId, userRole);

        // 3. 성공 응답 (200 OK, 명세서 요구사항에 따라 data는 비워서 보냄)
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null));
    }
}