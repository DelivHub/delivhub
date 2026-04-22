package com.sparta.delivhub.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestUpdatePaymentStatusDTO {

    @NotBlank(message = "변경할 상태값을 입력해주세요. (예: CANCELLED)")
    private String status;

}
