package com.sparta.delivhub.domain.payment.dto;

import com.sparta.delivhub.domain.payment.entity.Payment;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class MyPaymentDto {
    private UUID paymentId;
    private Long amount;
    private String status;
    private LocalDateTime createdAt;

    public MyPaymentDto(Payment payment) {
        this.paymentId = payment.getId();
        this.amount = payment.getAmount();
        this.status = payment.getStatus().name(); // Enum 타입을 String으로 변환
        this.createdAt = payment.getCreatedAt();
    }
}
