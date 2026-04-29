package com.sparta.delivhub.domain.area.controller;

import com.sparta.delivhub.common.dto.ApiResponse;
import com.sparta.delivhub.common.util.PageableUtils;
import com.sparta.delivhub.domain.area.dto.requset.AreaRequestDto;
import com.sparta.delivhub.domain.area.dto.response.AreaCityResponseDto;
import com.sparta.delivhub.domain.area.dto.response.AreaNameResponseDto;
import com.sparta.delivhub.domain.area.dto.response.AreaResponseDto;
import com.sparta.delivhub.domain.area.service.AreaService;
import com.sparta.delivhub.domain.store.dto.requset.StoreRequestDto;
import com.sparta.delivhub.domain.store.dto.response.StoreDetailResponseDto;
import com.sparta.delivhub.domain.store.dto.response.StoreListResponseDto;
import com.sparta.delivhub.domain.store.service.StoreService;
import com.sparta.delivhub.security.UserDetailsImpl;
import jakarta.validation.Valid;
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
public class AreaController {
    private final AreaService areaService;

    @PostMapping("/areas")
    public ApiResponse<AreaResponseDto> createStore(@Valid @RequestBody AreaRequestDto areaRequestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        AreaResponseDto id = areaService.createArea(areaRequestDto, userDetails.getUsername());
        return ApiResponse.created(id);
    }

    @GetMapping("/areas")
    public ApiResponse<List<AreaResponseDto>> getAllAreas(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "createdAt,DESC") String sort) {
        Pageable pageable = PageableUtils.of(page, size, sort);
        return ApiResponse.success(areaService.findAllAreas(pageable));
    }

    @GetMapping("/areas/{areaId}")
    public ApiResponse<AreaCityResponseDto> getArea(@PathVariable("areaId") UUID areaId) {
        return ApiResponse.success(areaService.findArea(areaId));
    }

    @PutMapping("/areas/{areaId}")
    public ApiResponse<AreaNameResponseDto> updateArea(@PathVariable("areaId") UUID areaId, @Valid @RequestBody AreaRequestDto areaRequestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        AreaNameResponseDto id = areaService.updateArea(areaId, areaRequestDto, userDetails.getUsername());
        return ApiResponse.success(id);
    }

    @DeleteMapping("/areas/{areaId}")
    public ApiResponse<AreaNameResponseDto> deleteArea(@PathVariable("areaId") UUID areaId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        AreaNameResponseDto id = areaService.deleteArea(areaId, userDetails.getUsername());
        return ApiResponse.success(id);
    }

}
