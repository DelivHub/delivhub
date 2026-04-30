package com.sparta.delivhub.domain.option.controller;

import com.sparta.delivhub.common.BaseControllerTest;
import com.sparta.delivhub.domain.option.dto.ResponseOptionDto;
import com.sparta.delivhub.domain.option.entity.OptionType;
import com.sparta.delivhub.domain.option.service.OptionService;
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
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OptionControllerTest extends BaseControllerTest {

    @MockitoBean
    private OptionService optionService;

    private final UUID menuId = UUID.randomUUID();
    private final UUID optionId = UUID.randomUUID();

    private String baseUrl() {
        return "/api/v1/menus/" + menuId + "/options";
    }

    @Test
    @DisplayName("옵션 등록 성공 - OWNER 권한")
    void createOption_Success() throws Exception {
        mockUserSetup("owner1", UserRole.OWNER);
        Map<String, Object> request = Map.of(
                "name", "사이즈",
                "type", OptionType.SINGLE.name(),
                "items", List.of(Map.of("name", "Large", "extraPrice", 1000L))
        );
        given(optionService.createOption(any(), any(), anyString())).willReturn(mock(ResponseOptionDto.class));

        mockMvc.perform(post(baseUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("옵션 등록 실패 - 미인증 사용자")
    void createOption_Unauthenticated() throws Exception {
        Map<String, Object> request = Map.of(
                "name", "사이즈",
                "type", OptionType.SINGLE.name(),
                "items", List.of(Map.of("name", "Large", "extraPrice", 1000L))
        );

        mockMvc.perform(post(baseUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("옵션 등록 실패 - 필수값 누락 시 400")
    void createOption_BadRequest() throws Exception {
        mockUserSetup("owner1", UserRole.OWNER);
        Map<String, Object> invalidRequest = Map.of("name", "사이즈");

        mockMvc.perform(post(baseUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("옵션 목록 조회 성공")
    void getOptions_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        given(optionService.getOptions(any())).willReturn(List.of());

        mockMvc.perform(get(baseUrl()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("옵션 수정 성공 - OWNER 권한")
    void updateOption_Success() throws Exception {
        mockUserSetup("owner1", UserRole.OWNER);
        Map<String, Object> request = Map.of("name", "변경된 사이즈");
        given(optionService.updateOption(any(), any(), any(), anyString())).willReturn(mock(ResponseOptionDto.class));

        mockMvc.perform(patch(baseUrl() + "/" + optionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("옵션 수정 실패 - 미인증 사용자")
    void updateOption_Unauthenticated() throws Exception {
        Map<String, Object> request = Map.of("name", "변경된 사이즈");

        mockMvc.perform(patch(baseUrl() + "/" + optionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("옵션 삭제 성공 - OWNER 권한")
    void deleteOption_Success() throws Exception {
        mockUserSetup("owner1", UserRole.OWNER);
        willDoNothing().given(optionService).deleteOption(any(), any(), anyString());

        mockMvc.perform(delete(baseUrl() + "/" + optionId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("옵션 삭제 실패 - 미인증 사용자")
    void deleteOption_Unauthenticated() throws Exception {
        mockMvc.perform(delete(baseUrl() + "/" + optionId))
                .andExpect(status().is4xxClientError());
    }
}
