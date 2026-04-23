package com.sparta.delivhub.domain.ai.controller;

import com.sparta.delivhub.common.dto.ApiResponse;
import com.sparta.delivhub.common.util.PageableUtils;
import com.sparta.delivhub.domain.ai.dto.ResponseAiLogDto;
import com.sparta.delivhub.domain.ai.service.AiLogService;
import com.sparta.delivhub.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class AiLogController {
    private final AiLogService aiLogService;

    // AI 로그 전체 조회
    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<Page<ResponseAiLogDto>>> getAiLogs(
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,DESC") String sort,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Pageable pageable = PageableUtils.of(page, size, sort);
        Page<ResponseAiLogDto> response = aiLogService.getAiLogs(userDetails.getUsername(),username, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // AI 로그 단건 조회
    @GetMapping("/logs/{logId}")
    public ResponseEntity<ApiResponse<ResponseAiLogDto>> getAiLog(
            @PathVariable UUID logId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ResponseAiLogDto response = aiLogService.getAiLog(userDetails.getUsername(), logId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
