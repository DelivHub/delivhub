package com.sparta.delivhub.domain.user.controller;

import com.sparta.delivhub.common.BaseControllerTest;
import com.sparta.delivhub.common.dto.PageResponse;
import com.sparta.delivhub.domain.payment.dto.MyPaymentListResponseDto;
import com.sparta.delivhub.domain.payment.service.PaymentService;
import com.sparta.delivhub.domain.user.dto.UserResponse;
import com.sparta.delivhub.domain.user.entity.UserRole;
import com.sparta.delivhub.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends BaseControllerTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PaymentService paymentService;

    private static final String BASE_URL = "/api/v1/users";

    @Test
    @DisplayName("내 결제 목록 조회 성공 - 인증된 사용자")
    void getMyPayments_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        given(paymentService.getMyPayments(anyString(), any())).willReturn(mock(MyPaymentListResponseDto.class));

        mockMvc.perform(get(BASE_URL + "/payments"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("내 결제 목록 조회 실패 - 미인증 사용자")
    void getMyPayments_Unauthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL + "/payments"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("사용자 목록 조회 성공 - MANAGER 권한")
    void getUsers_AsManager_Success() throws Exception {
        mockUserSetup("manager1", UserRole.MANAGER);
        given(userService.getUsers(any(), any(), any())).willReturn(mock(PageResponse.class));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("사용자 목록 조회 성공 - MASTER 권한")
    void getUsers_AsMaster_Success() throws Exception {
        mockUserSetup("master1", UserRole.MASTER);
        given(userService.getUsers(any(), any(), any())).willReturn(mock(PageResponse.class));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("사용자 목록 조회 실패 - CUSTOMER 권한 접근 금지")
    void getUsers_AsCustomer_Forbidden() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("사용자 목록 조회 실패 - 미인증 사용자")
    void getUsers_Unauthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("사용자 단건 조회 성공 - 본인 조회")
    void getUser_Self_Success() throws Exception {
        mockUserSetup("user1", UserRole.CUSTOMER);
        given(userService.getUser(anyString())).willReturn(mock(UserResponse.class));

        mockMvc.perform(get(BASE_URL + "/user1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("사용자 단건 조회 성공 - MASTER 권한으로 타인 조회")
    void getUser_AsManager_Success() throws Exception {
        mockUserSetup("master1", UserRole.MASTER);
        given(userService.getUser(anyString())).willReturn(mock(UserResponse.class));

        mockMvc.perform(get(BASE_URL + "/otheruser"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("사용자 단건 조회 실패 - 미인증 사용자")
    void getUser_Unauthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL + "/user1"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("사용자 정보 수정 성공 - 본인 수정")
    void updateUser_Self_Success() throws Exception {
        mockUserSetup("user1", UserRole.CUSTOMER);
        Map<String, Object> request = Map.of("nickname", "newNickname");
        given(userService.updateUser(anyString(), any())).willReturn(mock(UserResponse.class));

        mockMvc.perform(put(BASE_URL + "/user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("사용자 정보 수정 실패 - 타인 수정 시도")
    void updateUser_OtherUser_Forbidden() throws Exception {
        mockUserSetup("user1", UserRole.CUSTOMER);
        Map<String, Object> request = Map.of("nickname", "newNickname");

        mockMvc.perform(put(BASE_URL + "/otheruser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("권한 변경 성공 - MASTER 권한")
    void updateRole_AsMaster_Success() throws Exception {
        mockUserSetup("master1", UserRole.MASTER);
        Map<String, Object> request = Map.of("role", "OWNER");
        given(userService.updateRole(anyString(), any())).willReturn(mock(UserResponse.class));

        mockMvc.perform(put(BASE_URL + "/targetuser/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("권한 변경 실패 - CUSTOMER가 권한 변경 시도")
    void updateRole_AsCustomer_Forbidden() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        Map<String, Object> request = Map.of("role", "OWNER");

        mockMvc.perform(put(BASE_URL + "/targetuser/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("사용자 삭제 성공 - MANAGER 권한")
    void deleteUser_AsManager_Success() throws Exception {
        mockUserSetup("manager1", UserRole.MANAGER);
        willDoNothing().given(userService).deleteUser(anyString(), anyString());

        mockMvc.perform(delete(BASE_URL + "/targetuser"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("사용자 삭제 실패 - CUSTOMER가 삭제 시도")
    void deleteUser_AsCustomer_Forbidden() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);

        mockMvc.perform(delete(BASE_URL + "/targetuser"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("사용자 삭제 실패 - 미인증 사용자")
    void deleteUser_Unauthenticated() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/targetuser"))
                .andExpect(status().is4xxClientError());
    }
}
