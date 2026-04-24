package com.sparta.delivhub.domain.store.controller;

import com.sparta.delivhub.common.dto.ApiResponse;
import com.sparta.delivhub.domain.review.service.ReviewService;
import com.sparta.delivhub.domain.store.dto.requset.StoreRequestDto;
import com.sparta.delivhub.domain.store.dto.response.StoreDetailResponseDto;
import com.sparta.delivhub.domain.store.dto.response.StoreIdResponseDto;
import com.sparta.delivhub.domain.store.dto.response.StoreListResponseDto;
import com.sparta.delivhub.domain.review.dto.StoreReviewPageResponseDto;
import com.sparta.delivhub.domain.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StoreController {
    private final StoreService storeService;
    private final ReviewService reviewService;

    @PostMapping("/stores")
    public ApiResponse<StoreIdResponseDto> createStore(@RequestBody StoreRequestDto storeRequestDto) {
        StoreIdResponseDto id = storeService.createStore(storeRequestDto);
        return ApiResponse.success(id);
    }

    @GetMapping("/stores")
    public ApiResponse<List<StoreListResponseDto>> getAllStores() {
        return ApiResponse.success(storeService.findAllStores());
    }

    @GetMapping("/stores/{storeId}")
    public ApiResponse<StoreDetailResponseDto> getStore(@PathVariable("storeId") UUID storeId) {
        return ApiResponse.success(storeService.findStore(storeId));
    }

    @PutMapping("/stores/{storeId}")
    public ApiResponse<StoreIdResponseDto> updateStore(@PathVariable("storeId") UUID storeId, @RequestBody StoreRequestDto storeRequestDto) {
        StoreIdResponseDto id = storeService.updateStore(storeId, storeRequestDto);
        return ApiResponse.success(id);
    }

    @DeleteMapping("/stores/{storeId}")
    public ApiResponse<StoreIdResponseDto> deleteStore(@PathVariable("storeId") UUID storeId) {
        StoreIdResponseDto id = storeService.deleteStore(storeId, "ADMIN");
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
}
