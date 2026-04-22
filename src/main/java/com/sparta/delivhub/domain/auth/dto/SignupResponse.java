package com.sparta.delivhub.domain.auth.dto;

import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.entity.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SignupResponse {

    private String username;
    private String nickname;
    private String email;
    private UserRole role;
    private LocalDateTime createdAt;

    public static SignupResponse from(User user) {
        return SignupResponse.builder()
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .role(user.getUserRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
