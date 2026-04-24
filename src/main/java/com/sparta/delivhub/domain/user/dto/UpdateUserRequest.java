package com.sparta.delivhub.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class UpdateUserRequest {

    private String nickname;

    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "유효한 이메일 형식이 아닙니다."
    )
    private String email;

    private Boolean isPublic;
}
