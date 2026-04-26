package com.sparta.delivhub.domain.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor
public class RequestPaymentDTO {

    @NotNull(message = "주문 ID는 필수입니다.")
    private UUID orderId;

    @NotNull(message = "결제 금액은 필수입니다.")
    @Min(value = 1, message = "결제 금액은 0원보다 커야 합니다.")
    private Long amount;

    @NotBlank(message = "결제 수단은 필수입니다.")
    private String paymentMethod;

    private String status;


}