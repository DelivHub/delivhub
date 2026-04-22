package com.sparta.delivhub.domain.auth.dto;

import com.sparta.delivhub.domain.user.entity.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String accessToken;
    private String username;
    private UserRole role;
}
