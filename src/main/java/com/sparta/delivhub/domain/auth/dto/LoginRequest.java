package com.sparta.delivhub.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
