package com.sparta.delivhub.domain.category.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.domain.category.dto.requset.CategoryRequestDto;
import com.sparta.delivhub.domain.category.dto.response.CategoryIdResponseDto;
import com.sparta.delivhub.domain.category.dto.response.CategoryNameResponseDto;
import com.sparta.delivhub.domain.category.entity.Category;
import com.sparta.delivhub.domain.category.repository.CategoryRepository;
import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.entity.UserRole;
import com.sparta.delivhub.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("카테고리 생성 성공")
    void createCategory_Success() {
        // given
        String userId = "masterUser";
        CategoryRequestDto request = new CategoryRequestDto("한식");
        User master = User.builder().username(userId).userRole(UserRole.MASTER).build();

        given(userRepository.findByUsernameAndDeletedAtIsNull(userId)).willReturn(Optional.of(master));
        Category savedCategory = Category.builder().id(UUID.randomUUID()).name(request.getName()).build();
        given(categoryRepository.save(any(Category.class))).willReturn(savedCategory);

        // when
        CategoryNameResponseDto response = categoryService.createCategory(request, userId);

        // then
        assertThat(response.getName()).isEqualTo(savedCategory.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("카테고리 생성 실패")
    void createCategory_Fail_AccessDenied() {
        // given
        String userId = "customerUser";
        CategoryRequestDto request = new CategoryRequestDto("중식");
        User customer = User.builder().username(userId).userRole(UserRole.CUSTOMER).build();

        given(userRepository.findByUsernameAndDeletedAtIsNull(userId)).willReturn(Optional.of(customer));

        // when & then
        assertThatThrownBy(() -> categoryService.createCategory(request, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("카테고리 전체 목록 조회")
    void findAllCategory_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Category category = Category.builder().name("일식").build();
        Page<Category> categoryPage = new PageImpl<>(List.of(category));

        given(categoryRepository.findAll(pageable)).willReturn(categoryPage);

        // when
        List<CategoryNameResponseDto> result = categoryService.findAllCategory(pageable);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("일식");
        verify(categoryRepository).findAll(pageable);
    }

    @Test
    @DisplayName("카테고리 상세 조회 성공")
    void findCategory_Success() {
        // given
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder()
                .id(categoryId)
                .name("양식")
                .build();
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // when
        CategoryNameResponseDto result = categoryService.findCategory(categoryId);

        // then
        assertThat(result.getName()).isEqualTo("양식");
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void updateCategory_Success() {
        // given
        UUID categoryId = UUID.randomUUID();
        String userId = "masterUser";
        User master = User.builder().username(userId).userRole(UserRole.MASTER).build();
        Category category = Category.builder().id(categoryId).name("한식").build();
        CategoryRequestDto request = new CategoryRequestDto("중식");

        given(userRepository.findByUsernameAndDeletedAtIsNull(userId)).willReturn(Optional.of(master));
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // when
        CategoryIdResponseDto response = categoryService.updateCategory(categoryId, request, userId);

        // then
        assertThat(response.getCategoryId()).isEqualTo(categoryId);
    }

    @Test
    @DisplayName("카테고리 수정 실패 - 존재하지 않는 카테고리")
    void updateCategory_Fail_CategoryNotFound() {
        // given
        UUID categoryId = UUID.randomUUID();
        String userId = "masterUser";
        User master = User.builder().username(userId).userRole(UserRole.MASTER).build();
        CategoryRequestDto request = new CategoryRequestDto("중식");

        given(userRepository.findByUsernameAndDeletedAtIsNull(userId)).willReturn(Optional.of(master));
        given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategory(categoryId, request, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.CATEGORY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void deleteCategory_Success() {
        // given
        UUID categoryId = UUID.randomUUID();
        String userId = "adminUser";
        Category category = Category.builder().id(categoryId).name("분식").build();

        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // when
        categoryService.deleteCategory(categoryId, userId);

        // then
        assertThat(category.getDeletedAt()).isNotNull();
        assertThat(category.getDeletedBy()).isEqualTo(userId);
    }
}