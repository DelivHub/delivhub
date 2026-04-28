package com.sparta.delivhub.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReissueResponse {

    private String accessToken;
}
