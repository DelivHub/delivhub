package com.sparta.delivhub.domain.menu.controller;

import com.sparta.delivhub.common.BaseControllerTest;
import com.sparta.delivhub.domain.menu.dto.ResponseMenuDto;
import com.sparta.delivhub.domain.menu.service.MenuService;
import com.sparta.delivhub.domain.user.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MenuControllerTest extends BaseControllerTest {

    @MockitoBean
    private MenuService menuService;

    private final UUID storeId = UUID.randomUUID();
    private final UUID menuId = UUID.randomUUID();

    @Test
    @DisplayName("메뉴 등록 성공 - OWNER 권한")
    void createMenu_Success() throws Exception {
        mockUserSetup("owner1", UserRole.OWNER);
        Map<String, Object> request = Map.of(
                "name", "테스트 메뉴",
                "price", 10000
        );
        given(menuService.createMenu(any(), any(), anyString())).willReturn(mock(ResponseMenuDto.class));

        mockMvc.perform(post("/api/v1/stores/" + storeId + "/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("메뉴 등록 실패 - 미인증 사용자")
    void createMenu_Unauthenticated() throws Exception {
        Map<String, Object> request = Map.of("name", "테스트 메뉴", "price", 10000);

        mockMvc.perform(post("/api/v1/stores/" + storeId + "/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("메뉴 등록 실패 - 필수값 누락 시 400")
    void createMenu_BadRequest() throws Exception {
        mockUserSetup("owner1", UserRole.OWNER);
        Map<String, Object> invalidRequest = Map.of();

        mockMvc.perform(post("/api/v1/stores/" + storeId + "/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("메뉴 목록 조회 성공")
    void getMenus_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        given(menuService.getMenus(any(), any(), anyBoolean())).willReturn(Page.empty());

        mockMvc.perform(get("/api/v1/stores/" + storeId + "/menus"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("메뉴 단건 조회 성공")
    void getMenu_Success() throws Exception {
        mockUserSetup("customer1", UserRole.CUSTOMER);
        given(menuService.getMenu(any())).willReturn(mock(ResponseMenuDto.class));

        mockMvc.perform(get("/api/v1/menus/" + menuId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("메뉴 수정 성공 - OWNER 권한")
    void updateMenu_Success() throws Exception {
        mockUserSetup("owner1", UserRole.OWNER);
        Map<String, Object> request = Map.of("name", "수정된 메뉴", "price", 12000);
        given(menuService.updateMenu(any(), any(), anyString())).willReturn(mock(ResponseMenuDto.class));

        mockMvc.perform(patch("/api/v1/menus/" + menuId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("메뉴 수정 실패 - 미인증 사용자")
    void updateMenu_Unauthenticated() throws Exception {
        Map<String, Object> request = Map.of("name", "수정된 메뉴");

        mockMvc.perform(patch("/api/v1/menus/" + menuId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("메뉴 숨김 처리 성공 - OWNER 권한")
    void updateMenuHidden_Success() throws Exception {
        mockUserSetup("owner1", UserRole.OWNER);
        Map<String, Object> request = Map.of("isHidden", true);
        willDoNothing().given(menuService).updateMenuHidden(any(), any(), anyString());

        mockMvc.perform(patch("/api/v1/menus/" + menuId + "/hidden")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("메뉴 숨김 처리 실패 - 필수값 누락 시 400")
    void updateMenuHidden_BadRequest() throws Exception {
        mockUserSetup("owner1", UserRole.OWNER);
        Map<String, Object> invalidRequest = Map.of();

        mockMvc.perform(patch("/api/v1/menus/" + menuId + "/hidden")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("메뉴 삭제 성공 - OWNER 권한")
    void deleteMenu_Success() throws Exception {
        mockUserSetup("owner1", UserRole.OWNER);
        willDoNothing().given(menuService).deleteMenu(any(), anyString());

        mockMvc.perform(delete("/api/v1/menus/" + menuId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("메뉴 삭제 실패 - 미인증 사용자")
    void deleteMenu_Unauthenticated() throws Exception {
        mockMvc.perform(delete("/api/v1/menus/" + menuId))
                .andExpect(status().is4xxClientError());
    }
}
