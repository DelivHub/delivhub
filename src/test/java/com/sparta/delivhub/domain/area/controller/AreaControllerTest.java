package com.sparta.delivhub.domain.area.controller;

import com.sparta.delivhub.common.BaseControllerTest;
import com.sparta.delivhub.domain.area.dto.response.AreaCityResponseDto;
import com.sparta.delivhub.domain.area.dto.response.AreaIdResponseDto;
import com.sparta.delivhub.domain.area.service.AreaService;
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

class AreaControllerTest extends BaseControllerTest {

    @MockitoBean
    private AreaService areaService;

    private static final String BASE_URL = "/api/v1/areas";
    private final UUID areaId = UUID.randomUUID();

    @Test
    @DisplayName("지역 등록 성공 - MASTER 권한")
    void createArea_Success() throws Exception {
        mockUserSetup("master1", UserRole.MASTER);
        Map<String, Object> request = Map.of(
                "city", "서울",
                "district", "강남구",
                "name", "역삼동",
                "isHidden", false
        );
        given(areaService.createArea(any(), anyString())).willReturn(mock(AreaIdResponseDto.class));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("지역 등록 실패 - 미인증 사용자")
    void createArea_Unauthenticated() throws Exception {
        Map<String, Object> request = Map.of("city", "서울", "district", "강남구", "name", "역삼동", "isHidden", false);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("지역 등록 실패 - 필수값 누락 시 400")
    void createArea_BadRequest() throws Exception {
        mockUserSetup("master1", UserRole.MASTER);
        Map<String, Object> invalidRequest = Map.of("isHidden", false);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("지역 목록 조회 성공")
    void getAllAreas_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        given(areaService.findAllAreas(any())).willReturn(List.of());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("지역 단건 조회 성공")
    void getArea_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        given(areaService.findArea(any())).willReturn(mock(AreaCityResponseDto.class));

        mockMvc.perform(get(BASE_URL + "/" + areaId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("지역 수정 성공 - MASTER 권한")
    void updateArea_Success() throws Exception {
        mockUserSetup("master1", UserRole.MASTER);
        Map<String, Object> request = Map.of(
                "city", "서울",
                "district", "서초구",
                "name", "서초동",
                "isHidden", false
        );
        given(areaService.updateArea(any(), any(), anyString())).willReturn(mock(AreaIdResponseDto.class));

        mockMvc.perform(put(BASE_URL + "/" + areaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("지역 수정 실패 - 미인증 사용자")
    void updateArea_Unauthenticated() throws Exception {
        Map<String, Object> request = Map.of("city", "서울", "district", "서초구", "name", "서초동", "isHidden", false);

        mockMvc.perform(put(BASE_URL + "/" + areaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("지역 삭제 성공 - MASTER 권한")
    void deleteArea_Success() throws Exception {
        mockUserSetup("master1", UserRole.MASTER);
        given(areaService.deleteArea(any(), anyString())).willReturn(mock(AreaIdResponseDto.class));

        mockMvc.perform(delete(BASE_URL + "/" + areaId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("지역 삭제 실패 - 미인증 사용자")
    void deleteArea_Unauthenticated() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + areaId))
                .andExpect(status().is4xxClientError());
    }
}
