package com.sparta.delivhub.domain.auth.controller;

import com.sparta.delivhub.common.BaseControllerTest;
import com.sparta.delivhub.domain.auth.dto.LoginRequest;
import com.sparta.delivhub.domain.auth.dto.LoginResponse;
import com.sparta.delivhub.domain.auth.dto.SignupRequest;
import com.sparta.delivhub.domain.auth.dto.SignupResponse;
import com.sparta.delivhub.domain.auth.service.AuthService;
import com.sparta.delivhub.domain.user.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends BaseControllerTest {

    @MockitoBean
    private AuthService authService;

    private static final String BASE_URL = "/api/v1/auth";

    @Test
    @DisplayName("회원가입 성공 - 인증 불필요")
    void signup_Success() throws Exception {
        SignupRequest request = SignupRequest.builder()
                .username("newuser1")
                .password("Password1!")
                .nickname("닉네임")
                .email("test@test.com")
                .role(UserRole.CUSTOMER)
                .build();
        given(authService.signup(any())).willReturn(mock(SignupResponse.class));

        mockMvc.perform(post(BASE_URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("회원가입 실패 - 아이디 형식 불일치 시 400")
    void signup_InvalidUsername_BadRequest() throws Exception {
        SignupRequest request = SignupRequest.builder()
                .username("INVALID_ID!")
                .password("Password1!")
                .nickname("닉네임")
                .email("test@test.com")
                .role(UserRole.CUSTOMER)
                .build();

        mockMvc.perform(post(BASE_URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 형식 불일치 시 400")
    void signup_InvalidPassword_BadRequest() throws Exception {
        SignupRequest request = SignupRequest.builder()
                .username("newuser1")
                .password("weakpass")
                .nickname("닉네임")
                .email("test@test.com")
                .role(UserRole.CUSTOMER)
                .build();

        mockMvc.perform(post(BASE_URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 형식 불일치 시 400")
    void signup_InvalidEmail_BadRequest() throws Exception {
        SignupRequest request = SignupRequest.builder()
                .username("newuser1")
                .password("Password1!")
                .nickname("닉네임")
                .email("not-an-email")
                .role(UserRole.CUSTOMER)
                .build();

        mockMvc.perform(post(BASE_URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 성공 - 인증 불필요")
    void login_Success() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("user1")
                .password("Password1!")
                .build();
        given(authService.login(any())).willReturn(mock(LoginResponse.class));

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그인 실패 - 필수값 누락 시 400")
    void login_MissingFields_BadRequest() throws Exception {
        LoginRequest invalidRequest = LoginRequest.builder().build();

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그아웃 성공 - 인증된 사용자")
    void logout_Success() throws Exception {
        mockUserSetup("user1", UserRole.CUSTOMER);
        willDoNothing().given(authService).logout(anyString());

        mockMvc.perform(post(BASE_URL + "/logout"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그아웃 실패 - 미인증 사용자")
    void logout_Unauthenticated() throws Exception {
        mockMvc.perform(post(BASE_URL + "/logout"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("토큰 재발급 성공 - 인증 불필요")
    void reissue_Success() throws Exception {
        given(authService.reissue(anyString())).willReturn(mock(com.sparta.delivhub.domain.auth.dto.ReissueResponse.class));

        mockMvc.perform(post(BASE_URL + "/reissue")
                        .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isOk());
    }
}
