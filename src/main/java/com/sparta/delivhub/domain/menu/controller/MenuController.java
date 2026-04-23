package com.sparta.delivhub.domain.menu.controller;

import com.sparta.delivhub.common.dto.ApiResponse;
import com.sparta.delivhub.common.util.PageableUtils;
import com.sparta.delivhub.domain.menu.dto.CreateMenuDto;
import com.sparta.delivhub.domain.menu.dto.HiddenMenuDto;
import com.sparta.delivhub.domain.menu.dto.ResponseMenuDto;
import com.sparta.delivhub.domain.menu.dto.ResponseMenuListDto;
import com.sparta.delivhub.domain.menu.dto.UpdateMenuDto;
import com.sparta.delivhub.domain.menu.service.MenuService;
import com.sparta.delivhub.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MenuController {
    private final MenuService menuService;

    // 메뉴 등록
    @PostMapping("/stores/{storeId}/menus")
    public ResponseEntity<ApiResponse<ResponseMenuDto>> createMenu(
            @PathVariable UUID storeId,
            @Valid @RequestBody CreateMenuDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ResponseMenuDto response = menuService.createMenu(storeId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    // 메뉴 목록 조회
    @GetMapping("/stores/{storeId}/menus")
    public ResponseEntity<ApiResponse<Page<ResponseMenuListDto>>> getMenus(
            @PathVariable UUID storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,DESC") String sort) {
        Pageable pageable = PageableUtils.of(page, size, sort);
        Page<ResponseMenuListDto> response = menuService.getMenus(storeId, pageable, false);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 메뉴 단건 조회
    @GetMapping("/menus/{menuId}")
    public ResponseEntity<ApiResponse<ResponseMenuDto>> getMenu(
            @PathVariable UUID menuId) {
        ResponseMenuDto response = menuService.getMenu(menuId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 메뉴 수정
    @PatchMapping("/menus/{menuId}")
    public ResponseEntity<ApiResponse<ResponseMenuDto>> updateMenu(
            @PathVariable UUID menuId,
            @Valid @RequestBody UpdateMenuDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ResponseMenuDto response = menuService.updateMenu(menuId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 메뉴 숨김 처리
    @PatchMapping("/menus/{menuId}/hidden")
    public ResponseEntity<ApiResponse<Void>> updateMenuHidden(
            @PathVariable UUID menuId,
            @Valid @RequestBody HiddenMenuDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        menuService.updateMenuHidden(menuId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 메뉴 삭제
    @DeleteMapping("/menus/{menuId}")
    public ResponseEntity<ApiResponse<Void>> deleteMenu(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID menuId
    ) {
        menuService.deleteMenu(menuId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
