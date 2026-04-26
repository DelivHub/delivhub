package com.sparta.delivhub.domain.user.dto;

import com.sparta.delivhub.domain.user.entity.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateRoleRequest {

    @NotNull(message = "권한은 필수입니다.")
    private UserRole role;
}
