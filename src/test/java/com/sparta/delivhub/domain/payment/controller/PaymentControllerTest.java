package com.sparta.delivhub.domain.payment.controller;

import com.sparta.delivhub.common.BaseControllerTest;
import com.sparta.delivhub.domain.payment.dto.RequestPaymentDTO;
import com.sparta.delivhub.domain.payment.dto.RequestUpdatePaymentStatusDTO;
import com.sparta.delivhub.domain.payment.dto.ResponsePaymentDTO;
import com.sparta.delivhub.domain.payment.service.PaymentService;
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

class PaymentControllerTest extends BaseControllerTest {

    @MockitoBean
    private PaymentService paymentService;

    private static final String BASE_URL = "/api/v1/payments";

    @Test
    @DisplayName("결제 생성 성공 - CUSTOMER 인증된 사용자")
    void createPayment_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        RequestPaymentDTO request = RequestPaymentDTO.builder()
                .orderId(UUID.randomUUID())
                .amount(10000L)
                .paymentMethod("CARD")
                .build();
        given(paymentService.createPayment(any(), anyString())).willReturn(mock(ResponsePaymentDTO.class));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("결제 생성 실패 - 미인증 사용자 접근 시 401/403")
    void createPayment_Unauthenticated() throws Exception {
        RequestPaymentDTO request = RequestPaymentDTO.builder()
                .orderId(UUID.randomUUID())
                .amount(10000L)
                .paymentMethod("CARD")
                .build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("결제 생성 실패 - 필수값 누락 시 400")
    void createPayment_BadRequest() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        RequestPaymentDTO invalidRequest = RequestPaymentDTO.builder().build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("결제 단건 조회 성공")
    void getPayment_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        UUID paymentId = UUID.randomUUID();
        given(paymentService.getPayment(any(), anyString(), anyString())).willReturn(mock(ResponsePaymentDTO.class));

        mockMvc.perform(get(BASE_URL + "/" + paymentId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("결제 단건 조회 실패 - 미인증 사용자")
    void getPayment_Unauthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + UUID.randomUUID()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("결제 상태 수정 성공 - MANAGER 권한")
    void updatePaymentStatus_Success() throws Exception {
        mockUserSetup("manager1", UserRole.MANAGER);
        UUID paymentId = UUID.randomUUID();
        RequestUpdatePaymentStatusDTO request = RequestUpdatePaymentStatusDTO.builder()
                .status("CANCELLED")
                .build();
        given(paymentService.updatePaymentStatus(any(), anyString(), anyString()))
                .willReturn(mock(ResponsePaymentDTO.class));

        mockMvc.perform(patch(BASE_URL + "/" + paymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("결제 상태 수정 실패 - 미인증 사용자")
    void updatePaymentStatus_Unauthenticated() throws Exception {
        RequestUpdatePaymentStatusDTO request = RequestUpdatePaymentStatusDTO.builder()
                .status("CANCELLED")
                .build();

        mockMvc.perform(patch(BASE_URL + "/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("결제 삭제 성공")
    void deletePayment_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        UUID paymentId = UUID.randomUUID();
        willDoNothing().given(paymentService).deletePayment(any(), anyString());

        mockMvc.perform(delete(BASE_URL + "/" + paymentId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("결제 삭제 실패 - 미인증 사용자")
    void deletePayment_Unauthenticated() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + UUID.randomUUID()))
                .andExpect(status().is4xxClientError());
    }
}
