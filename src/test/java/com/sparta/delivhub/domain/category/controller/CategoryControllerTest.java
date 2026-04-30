package com.sparta.delivhub.domain.category.controller;

import com.sparta.delivhub.common.BaseControllerTest;
import com.sparta.delivhub.domain.category.dto.response.CategoryNameResponseDto;
import com.sparta.delivhub.domain.category.service.CategoryService;
import com.sparta.delivhub.domain.user.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryControllerTest extends BaseControllerTest {

    @MockitoBean
    private CategoryService categoryService;

    private static final String BASE_URL = "/api/v1/categories";
    private final UUID categoryId = UUID.randomUUID();

    @Test
    @DisplayName("카테고리 등록 성공 - MASTER 권한")
    void createCategory_Success() throws Exception {
        mockUserSetup("master1", UserRole.MASTER);
        Map<String, Object> request = Map.of("name", "한식", "isHidden", false);
        given(categoryService.createCategory(any(), anyString())).willReturn(mock(CategoryNameResponseDto.class));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("카테고리 등록 실패 - 미인증 사용자")
    void createCategory_Unauthenticated() throws Exception {
        Map<String, Object> request = Map.of("name", "한식", "isHidden", false);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("카테고리 등록 실패 - 필수값 누락 시 400")
    void createCategory_BadRequest() throws Exception {
        mockUserSetup("master1", UserRole.MASTER);
        Map<String, Object> invalidRequest = Map.of("isHidden", false);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("카테고리 목록 조회 성공")
    void getAllCategories_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        given(categoryService.findAllCategory(any())).willReturn(List.of());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("카테고리 단건 조회 성공")
    void getCategory_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        given(categoryService.findCategory(any())).willReturn(mock(CategoryNameResponseDto.class));

        mockMvc.perform(get(BASE_URL + "/" + categoryId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("카테고리 수정 성공 - MASTER 권한")
    void updateCategory_Success() throws Exception {
        mockUserSetup("master1", UserRole.MASTER);
        Map<String, Object> request = Map.of("name", "중식", "isHidden", false);
        given(categoryService.updateCategory(any(), any(), anyString())).willReturn(mock(CategoryNameResponseDto.class));

        mockMvc.perform(put(BASE_URL + "/" + categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("카테고리 수정 실패 - 미인증 사용자")
    void updateCategory_Unauthenticated() throws Exception {
        Map<String, Object> request = Map.of("name", "중식", "isHidden", false);

        mockMvc.perform(put(BASE_URL + "/" + categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("카테고리 삭제 성공 - MASTER 권한")
    void deleteCategory_Success() throws Exception {
        mockUserSetup("master1", UserRole.MASTER);
        given(categoryService.deleteCategory(any(), anyString())).willReturn(mock(CategoryNameResponseDto.class));

        mockMvc.perform(delete(BASE_URL + "/" + categoryId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("카테고리 삭제 실패 - 미인증 사용자")
    void deleteCategory_Unauthenticated() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + categoryId))
                .andExpect(status().is4xxClientError());
    }
}
