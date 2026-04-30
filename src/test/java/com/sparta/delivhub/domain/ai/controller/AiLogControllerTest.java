package com.sparta.delivhub.domain.ai.controller;

import com.sparta.delivhub.common.BaseControllerTest;
import com.sparta.delivhub.domain.ai.dto.ResponseAiLogDto;
import com.sparta.delivhub.domain.ai.service.AiLogService;
import com.sparta.delivhub.domain.user.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AiLogControllerTest extends BaseControllerTest {

    @MockitoBean
    private AiLogService aiLogService;

    private static final String BASE_URL = "/api/v1/ai/logs";

    @Test
    @DisplayName("AI 로그 전체 조회 성공")
    void getAiLogs_Success() throws Exception {
        mockUserSetup("master1", UserRole.MASTER);
        given(aiLogService.getAiLogs(anyString(), any(), any())).willReturn(Page.empty());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("AI 로그 전체 조회 실패 - 미인증 사용자")
    void getAiLogs_Unauthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("AI 로그 단건 조회 성공")
    void getAiLog_Success() throws Exception {
        mockUserSetup("master1", UserRole.MASTER);
        UUID logId = UUID.randomUUID();
        given(aiLogService.getAiLog(anyString(), any())).willReturn(mock(ResponseAiLogDto.class));

        mockMvc.perform(get(BASE_URL + "/" + logId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("AI 로그 단건 조회 실패 - 미인증 사용자")
    void getAiLog_Unauthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + UUID.randomUUID()))
                .andExpect(status().is4xxClientError());
    }
}
