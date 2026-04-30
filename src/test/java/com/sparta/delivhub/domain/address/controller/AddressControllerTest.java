package com.sparta.delivhub.domain.address.controller;

import com.sparta.delivhub.common.BaseControllerTest;
import com.sparta.delivhub.domain.address.dto.AddressResponse;
import com.sparta.delivhub.domain.address.service.AddressService;
import com.sparta.delivhub.common.dto.PageResponse;
import com.sparta.delivhub.domain.user.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AddressControllerTest extends BaseControllerTest {

    @MockitoBean
    private AddressService addressService;

    private static final String BASE_URL = "/api/v1/addresses";
    private final UUID addressId = UUID.randomUUID();

    @Test
    @DisplayName("주소 등록 성공 - 201 CREATED")
    void createAddress_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        Map<String, Object> request = Map.of(
                "address", "서울시 강남구",
                "detail", "101호",
                "zipCode", "12345"
        );
        given(addressService.createAddress(anyString(), any())).willReturn(mock(AddressResponse.class));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("주소 등록 실패 - 미인증 사용자")
    void createAddress_Unauthenticated() throws Exception {
        Map<String, Object> request = Map.of("address", "서울시 강남구", "detail", "101호", "zipCode", "12345");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("주소 등록 실패 - 필수값 누락 시 400")
    void createAddress_BadRequest() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        Map<String, Object> invalidRequest = Map.of("alias", "집");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주소 목록 조회 성공")
    void getAddresses_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        given(addressService.getAddresses(anyString(), any(), any())).willReturn(mock(PageResponse.class));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("주소 목록 조회 실패 - 미인증 사용자")
    void getAddresses_Unauthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("주소 단건 조회 성공")
    void getAddress_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        given(addressService.getAddress(anyString(), any())).willReturn(mock(AddressResponse.class));

        mockMvc.perform(get(BASE_URL + "/" + addressId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("주소 수정 성공")
    void updateAddress_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        Map<String, Object> request = Map.of(
                "address", "서울시 서초구",
                "detail", "202호",
                "zipCode", "67890"
        );
        given(addressService.updateAddress(anyString(), any(), any())).willReturn(mock(AddressResponse.class));

        mockMvc.perform(put(BASE_URL + "/" + addressId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("주소 수정 실패 - 미인증 사용자")
    void updateAddress_Unauthenticated() throws Exception {
        Map<String, Object> request = Map.of("address", "서울시 서초구", "detail", "202호", "zipCode", "67890");

        mockMvc.perform(put(BASE_URL + "/" + addressId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("주소 삭제 성공 - CUSTOMER 권한")
    void deleteAddress_AsCustomer_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        willDoNothing().given(addressService).deleteAddress(any(), anyString(), any());

        mockMvc.perform(delete(BASE_URL + "/" + addressId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("주소 삭제 성공 - MASTER 권한")
    void deleteAddress_AsMaster_Success() throws Exception {
        mockUserSetup("master1", UserRole.MASTER);
        willDoNothing().given(addressService).deleteAddress(any(), anyString(), any());

        mockMvc.perform(delete(BASE_URL + "/" + addressId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("주소 삭제 실패 - 미인증 사용자")
    void deleteAddress_Unauthenticated() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + addressId))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("기본 배송지 설정 성공")
    void setDefault_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        Map<String, Object> request = Map.of("isDefault", true);
        given(addressService.setDefault(anyString(), any(), any())).willReturn(mock(AddressResponse.class));

        mockMvc.perform(patch(BASE_URL + "/" + addressId + "/default")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("기본 배송지 설정 실패 - 미인증 사용자")
    void setDefault_Unauthenticated() throws Exception {
        Map<String, Object> request = Map.of("isDefault", true);

        mockMvc.perform(patch(BASE_URL + "/" + addressId + "/default")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }
}
