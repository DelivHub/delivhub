package com.sparta.delivhub.domain.category.controller;

import com.sparta.delivhub.common.dto.ApiResponse;
import com.sparta.delivhub.domain.category.dto.requset.CategoryRequestDto;
import com.sparta.delivhub.domain.category.dto.response.CategoryIdResponseDto;
import com.sparta.delivhub.domain.category.dto.response.CategoryNameResponseDto;
import com.sparta.delivhub.domain.category.service.CategoryService;
import com.sparta.delivhub.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.file.attribute.UserPrincipal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping("/categories")
    public ApiResponse<CategoryIdResponseDto> createCategory(@Valid @RequestBody CategoryRequestDto categoryRequestDto,  @AuthenticationPrincipal UserDetailsImpl userDetails) {
        CategoryIdResponseDto id = categoryService.createCategory(categoryRequestDto, userDetails.getUsername());
        return ApiResponse.success(id);
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryNameResponseDto>> getAllCategory() {
        return ApiResponse.success(categoryService.findAllCategory());
    }

    @GetMapping("/categories/{categoryId}")
    public ApiResponse<CategoryNameResponseDto> getCategory(@PathVariable("categoryId") UUID categoryId) {
        return ApiResponse.success(categoryService.findCategory(categoryId));
    }

    @PutMapping("/categories/{categoryId}")
    public ApiResponse<CategoryIdResponseDto> updateCategory(@PathVariable("categoryId") UUID categoryId, @Valid @RequestBody CategoryRequestDto categoryRequestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        CategoryIdResponseDto id = categoryService.updateCategory(categoryId, categoryRequestDto, userDetails.getUsername());
        return ApiResponse.success(id);
    }

    @DeleteMapping("/categories/{categoryId}")
    public ApiResponse<CategoryIdResponseDto> deleteCategory(@PathVariable("categoryId") UUID categoryId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        CategoryIdResponseDto id = categoryService.deleteCategory(categoryId, userDetails.getUsername());
        return ApiResponse.success(id);
    }

}
