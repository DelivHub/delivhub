package com.sparta.delivhub.domain.store.controller;

import com.sparta.delivhub.common.dto.ApiResponse;
import com.sparta.delivhub.domain.store.dto.requset.StoreRequestDto;
import com.sparta.delivhub.domain.store.dto.response.StoreDetailResponseDto;
import com.sparta.delivhub.domain.store.dto.response.StoreIdResponseDto;
import com.sparta.delivhub.domain.store.dto.response.StoreListResponseDto;
import com.sparta.delivhub.domain.store.service.StoreService;
import com.sparta.delivhub.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
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

}
