package com.sparta.delivhub.domain.category.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.common.util.AuthorizationUtils;
import com.sparta.delivhub.domain.category.dto.requset.CategoryRequestDto;
import com.sparta.delivhub.domain.category.dto.response.CategoryIdResponseDto;
import com.sparta.delivhub.domain.category.dto.response.CategoryNameResponseDto;
import com.sparta.delivhub.domain.category.entity.Category;
import com.sparta.delivhub.domain.category.repository.CategoryRepository;
import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public CategoryIdResponseDto createCategory(CategoryRequestDto categoryRequestDto, String userId) {
        User user = userRepository.findByUsernameAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        AuthorizationUtils.checkAdminPermission(user);

        Category category = Category.builder()
                .name(categoryRequestDto.getName())
                .build();

        categoryRepository.save(category);

        return CategoryIdResponseDto.builder()
                .categoryId(category.getId())
                .build();
    }

    public List<CategoryNameResponseDto> findAllCategory() {
        return categoryRepository.findAll().stream()
                .map(CategoryNameResponseDto::new)
                .toList();
    }

    public CategoryNameResponseDto findCategory(UUID id) {
        return categoryRepository.findById(id)
                .map(CategoryNameResponseDto::new)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    @Transactional
    public CategoryIdResponseDto updateCategory(UUID id,  CategoryRequestDto categoryRequestDto, String userId) {
        User user = userRepository.findByUsernameAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        AuthorizationUtils.checkAdminPermission(user);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        category.updateCategory(
                categoryRequestDto.getName()
        );

        return CategoryIdResponseDto.builder()
                .categoryId(category.getId())
                .build();
    }

    @Transactional
    public CategoryIdResponseDto deleteCategory(UUID id, String userId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        category.softDelete(userId);

        return CategoryIdResponseDto.builder()
                .categoryId(category.getId())
                .build();
    }

}
