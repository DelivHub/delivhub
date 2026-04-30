package com.sparta.delivhub.domain.store.controller;

import com.sparta.delivhub.common.BaseControllerTest;
import com.sparta.delivhub.domain.payment.dto.StorePaymentListResponseDto;
import com.sparta.delivhub.domain.payment.service.PaymentService;
import com.sparta.delivhub.domain.review.dto.StoreReviewPageResponseDto;
import com.sparta.delivhub.domain.review.service.ReviewService;
import com.sparta.delivhub.domain.store.dto.response.StoreDetailResponseDto;
import com.sparta.delivhub.domain.store.dto.response.StoreIdResponseDto;
import com.sparta.delivhub.domain.store.service.StoreService;
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

class StoreControllerTest extends BaseControllerTest {

    @MockitoBean
    private StoreService storeService;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private PaymentService paymentService;

    private static final String BASE_URL = "/api/v1/stores";
    private final UUID storeId = UUID.randomUUID();

    @Test
    @DisplayName("가게 등록 성공 - OWNER 권한")
    void createStore_Success() throws Exception {
        mockUserSetup("owner1", UserRole.OWNER);
        Map<String, Object> request = Map.of(
                "name", "테스트 가게",
                "categoryId", UUID.randomUUID().toString(),
                "areaId", UUID.randomUUID().toString(),
                "address", "서울시 강남구",
                "number", "02-1234-5678",
                "isHidden", false
        );
        given(storeService.createStore(any(), anyString())).willReturn(mock(StoreIdResponseDto.class));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("가게 등록 실패 - 미인증 사용자")
    void createStore_Unauthenticated() throws Exception {
        Map<String, Object> request = Map.of("name", "테스트 가게");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("가게 목록 조회 성공")
    void getAllStores_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        given(storeService.findAllStores(any())).willReturn(List.of());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("가게 단건 조회 성공")
    void getStore_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        given(storeService.findStore(any())).willReturn(mock(StoreDetailResponseDto.class));

        mockMvc.perform(get(BASE_URL + "/" + storeId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("가게 수정 성공 - OWNER 권한")
    void updateStore_Success() throws Exception {
        mockUserSetup("owner1", UserRole.OWNER);
        Map<String, Object> request = Map.of(
                "name", "수정된 가게",
                "categoryId", UUID.randomUUID().toString(),
                "areaId", UUID.randomUUID().toString(),
                "address", "서울시 서초구",
                "number", "02-9999-8888",
                "isHidden", false
        );
        given(storeService.updateStore(any(), any(), anyString())).willReturn(mock(StoreIdResponseDto.class));

        mockMvc.perform(put(BASE_URL + "/" + storeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("가게 수정 실패 - 미인증 사용자")
    void updateStore_Unauthenticated() throws Exception {
        Map<String, Object> request = Map.of("name", "수정된 가게");

        mockMvc.perform(put(BASE_URL + "/" + storeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("가게 삭제 성공 - OWNER 권한")
    void deleteStore_Success() throws Exception {
        mockUserSetup("owner1", UserRole.OWNER);
        given(storeService.deleteStore(any(), anyString())).willReturn(mock(StoreIdResponseDto.class));

        mockMvc.perform(delete(BASE_URL + "/" + storeId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("가게 삭제 실패 - 미인증 사용자")
    void deleteStore_Unauthenticated() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + storeId))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("가게 리뷰 목록 조회 성공")
    void getReviewsByStore_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        given(reviewService.getReviewsByStore(any(), any())).willReturn(mock(StoreReviewPageResponseDto.class));

        mockMvc.perform(get(BASE_URL + "/" + storeId + "/reviews"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("가게 결제 목록 조회 성공 - OWNER 권한")
    void getStorePayments_Success() throws Exception {
        mockUserSetup("owner1", UserRole.OWNER);
        given(paymentService.getStorePayments(any(), anyString(), any()))
                .willReturn(mock(StorePaymentListResponseDto.class));

        mockMvc.perform(get(BASE_URL + "/" + storeId + "/payments"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("가게 결제 목록 조회 실패 - 미인증 사용자")
    void getStorePayments_Unauthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + storeId + "/payments"))
                .andExpect(status().is4xxClientError());
    }
}
