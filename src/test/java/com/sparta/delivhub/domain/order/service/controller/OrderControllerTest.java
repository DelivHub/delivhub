package com.sparta.delivhub.domain.order.service.controller;

import com.sparta.delivhub.common.BaseControllerTest;
import com.sparta.delivhub.domain.order.dto.OrderRequestDto;
import com.sparta.delivhub.domain.order.dto.OrderResponseDto;
import com.sparta.delivhub.domain.order.entity.OrderType;
import com.sparta.delivhub.domain.user.entity.UserRole;
import com.sparta.delivhub.domain.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerTest extends BaseControllerTest {

    @MockitoBean
    private OrderService orderService;

    private final String BASE_URL = "/api/v1/orders";

    @Test
    @DisplayName("성공 테스트: 정상적인 주문 생성 시 200 OK를 반환해야 함")
    void createOrder_Success() throws Exception {
        // Given
        mockUserSetup("tester", UserRole.CUSTOMER);
        OrderRequestDto requestDto = OrderRequestDto.builder()
                .storeId(UUID.randomUUID())
                .addressId(UUID.randomUUID())
                .items(List.of(OrderRequestDto.OrderItemRequestDto.builder().menuId(UUID.randomUUID()).quantity(1).build()))
                .orderType(OrderType.ONLINE)
                .build();

        given(orderService.createOrder(any(), anyString())).willReturn(mock(OrderResponseDto.class));

        // When & Then: URL을 /api/v1/orders 로 정확히 요청
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("보안 테스트: 인증되지 않은 사용자가 주문 시 4xx 에러를 반환해야 함")
    void createOrder_Unauthenticated() throws Exception {
        OrderRequestDto requestDto = OrderRequestDto.builder().build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("실패 테스트: 필수 값이 누락된 주문 요청 시 400 Bad Request를 반환해야 함")
    void createOrder_BadRequest() throws Exception {
        mockUserSetup("tester", UserRole.CUSTOMER);
        OrderRequestDto invalidRequest = OrderRequestDto.builder()
                .orderType(OrderType.ONLINE)
                .build(); // storeId, addressId, items 모두 누락

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("조회 테스트: 주문 목록 조회 API가 정상 작동해야 함")
    void getOrders_Success() throws Exception {
        mockUserSetup("admin", UserRole.MASTER);

        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("주문 단건 조회 성공")
    void getOrder_Success() throws Exception {
        mockUserSetup("tester", UserRole.CUSTOMER);
        UUID orderId = UUID.randomUUID();
        given(orderService.getOrder(any(), anyString(), anyString())).willReturn(mock(OrderResponseDto.class));

        mockMvc.perform(get(BASE_URL + "/" + orderId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("주문 단건 조회 실패 - 미인증 사용자")
    void getOrder_Unauthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + UUID.randomUUID()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("주문 요청사항 수정 성공")
    void updateRequest_Success() throws Exception {
        mockUserSetup("tester", UserRole.CUSTOMER);
        UUID orderId = UUID.randomUUID();
        given(orderService.updateRequest(any(), anyString(), anyString())).willReturn(mock(OrderResponseDto.class));

        mockMvc.perform(put(BASE_URL + "/" + orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"문 앞에 두세요\""))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("주문 상태 변경 성공")
    void updateStatus_Success() throws Exception {
        mockUserSetup("owner1", UserRole.OWNER);
        UUID orderId = UUID.randomUUID();
        given(orderService.updateStatus(any(), any(), anyString(), anyString())).willReturn(mock(OrderResponseDto.class));

        mockMvc.perform(patch(BASE_URL + "/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"DELIVERING\""))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("주문 취소 성공")
    void cancelOrder_Success() throws Exception {
        mockUserSetup("tester", UserRole.CUSTOMER);
        UUID orderId = UUID.randomUUID();
        given(orderService.cancelOrder(any(), anyString(), anyString())).willReturn(mock(OrderResponseDto.class));

        mockMvc.perform(patch(BASE_URL + "/" + orderId + "/cancel"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("주문 삭제 성공 - MASTER 권한")
    void deleteOrder_Success() throws Exception {
        mockUserSetup("master1", UserRole.MASTER);
        UUID orderId = UUID.randomUUID();
        willDoNothing().given(orderService).deleteOrder(any(), anyString());

        mockMvc.perform(delete(BASE_URL + "/" + orderId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("주문 삭제 실패 - 미인증 사용자")
    void deleteOrder_Unauthenticated() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + UUID.randomUUID()))
                .andExpect(status().is4xxClientError());
    }
}
