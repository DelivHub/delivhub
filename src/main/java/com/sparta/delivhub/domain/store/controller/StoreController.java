package com.sparta.delivhub.domain.store.controller;

import com.sparta.delivhub.common.dto.ApiResponse;
import com.sparta.delivhub.domain.store.dto.requset.StoreRequestDto;
import com.sparta.delivhub.domain.store.dto.response.StoreDetailResponseDto;
import com.sparta.delivhub.domain.store.dto.response.StoreIdResponseDto;
import com.sparta.delivhub.domain.store.dto.response.StoreListResponseDto;
import com.sparta.delivhub.domain.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StoreController {
    private final StoreService storeService;

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

}
