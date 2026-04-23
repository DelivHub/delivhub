package com.sparta.delivhub.domain.review.controller;

import com.sparta.delivhub.common.dto.ApiResponse;
import com.sparta.delivhub.domain.review.dto.MyReviewListResponseDto;
import com.sparta.delivhub.domain.review.dto.ReviewRequestDto;
import com.sparta.delivhub.domain.review.dto.ReviewResponseDto;
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
}