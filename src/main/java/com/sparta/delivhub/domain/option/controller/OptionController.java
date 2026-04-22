package com.sparta.delivhub.domain.option.controller;

import com.sparta.delivhub.common.dto.ApiResponse;
import com.sparta.delivhub.domain.option.dto.CreateOptionDto;
import com.sparta.delivhub.domain.option.dto.ResponseOptionDto;
import com.sparta.delivhub.domain.option.dto.UpdateOptionDto;
import com.sparta.delivhub.domain.option.service.OptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/menus/{menuId}/options")
public class OptionController {
    private final OptionService optionService;

    // 옵션 등록
    @PostMapping
    public ResponseEntity<ApiResponse<ResponseOptionDto>> createOption(
            @PathVariable UUID menuId,
            @Valid @RequestBody CreateOptionDto request) {
        ResponseOptionDto response = optionService.createOption(menuId, request, null);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 옵션 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<ResponseOptionDto>>> getOptions(
            @PathVariable UUID menuId) {
        List<ResponseOptionDto> response = optionService.getOptions(menuId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 옵션 수정
    @PatchMapping("/{optionId}")
    public ResponseEntity<ApiResponse<ResponseOptionDto>> updateOption(
            @PathVariable UUID menuId,
            @PathVariable UUID optionId,
            @Valid @RequestBody UpdateOptionDto request) {
        ResponseOptionDto response = optionService.updateOption(menuId, optionId, request, null);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 옵션 삭제
    @DeleteMapping("/{optionId}")
    public ResponseEntity<ApiResponse<Void>> deleteOption(
            @PathVariable UUID menuId,
            @PathVariable UUID optionId) {
        optionService.deleteOption(menuId, optionId, null);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
