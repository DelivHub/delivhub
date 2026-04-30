package com.sparta.delivhub.domain.review.controller;

import com.sparta.delivhub.common.BaseControllerTest;
import com.sparta.delivhub.domain.review.dto.MyReviewListResponseDto;
import com.sparta.delivhub.domain.review.dto.ReviewRequestDto;
import com.sparta.delivhub.domain.review.dto.ReviewResponseDto;
import com.sparta.delivhub.domain.review.dto.ReviewUpdateRequestDto;
import com.sparta.delivhub.domain.review.dto.StoreReviewListResponseDto;
import com.sparta.delivhub.domain.review.service.ReviewService;
import com.sparta.delivhub.domain.user.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReviewControllerTest extends BaseControllerTest {

    @MockitoBean
    private ReviewService reviewService;

    private static final String BASE_URL = "/api/v1/reviews";
    private final UUID reviewId = UUID.randomUUID();

    @Test
    @DisplayName("리뷰 등록 성공 - 201 CREATED")
    void createReview_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        ReviewRequestDto request = ReviewRequestDto.builder()
                .orderId(UUID.randomUUID())
                .storeId(UUID.randomUUID())
                .rating(4)
                .content("맛있어요")
                .build();
        given(reviewService.createReview(any(), anyString(), anyString())).willReturn(mock(ReviewResponseDto.class));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 미인증 사용자")
    void createReview_Unauthenticated() throws Exception {
        ReviewRequestDto request = ReviewRequestDto.builder()
                .orderId(UUID.randomUUID())
                .storeId(UUID.randomUUID())
                .rating(4)
                .content("맛있어요")
                .build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 별점 범위 초과 시 400")
    void createReview_InvalidRating_BadRequest() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        ReviewRequestDto request = ReviewRequestDto.builder()
                .orderId(UUID.randomUUID())
                .storeId(UUID.randomUUID())
                .rating(6)
                .content("맛있어요")
                .build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 필수값 누락 시 400")
    void createReview_MissingFields_BadRequest() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        ReviewRequestDto invalidRequest = ReviewRequestDto.builder().build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("내 리뷰 목록 조회 성공")
    void getMyReviews_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        given(reviewService.getMyReviews(anyString(), anyString(), any())).willReturn(mock(MyReviewListResponseDto.class));

        mockMvc.perform(get(BASE_URL + "/my"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("내 리뷰 목록 조회 실패 - 미인증 사용자")
    void getMyReviews_Unauthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL + "/my"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("전체 리뷰 목록 조회 성공")
    void getAllStoreReviews_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        given(reviewService.getAllStoreReviews(any())).willReturn(mock(StoreReviewListResponseDto.class));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("리뷰 수정 성공")
    void updateReview_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        ReviewUpdateRequestDto request = ReviewUpdateRequestDto.builder()
                .rating(5)
                .content("정말 맛있어요")
                .build();
        given(reviewService.updateReview(any(), any(), anyString(), anyString())).willReturn(mock(ReviewResponseDto.class));

        mockMvc.perform(put(BASE_URL + "/" + reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 미인증 사용자")
    void updateReview_Unauthenticated() throws Exception {
        ReviewUpdateRequestDto request = ReviewUpdateRequestDto.builder()
                .rating(5)
                .content("정말 맛있어요")
                .build();

        mockMvc.perform(put(BASE_URL + "/" + reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    void deleteReview_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        willDoNothing().given(reviewService).deleteReview(any(), anyString(), anyString());

        mockMvc.perform(delete(BASE_URL + "/" + reviewId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 미인증 사용자")
    void deleteReview_Unauthenticated() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + reviewId))
                .andExpect(status().is4xxClientError());
    }
}
