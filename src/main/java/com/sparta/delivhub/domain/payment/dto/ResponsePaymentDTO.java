package com.sparta.delivhub.domain.payment.dto;

import com.sparta.delivhub.domain.payment.entity.Payment;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ResponsePaymentDTO {
    private UUID paymentId;
    private UUID orderId;
    private Integer amount;
    private String status;
    private LocalDateTime createdAt;

    // 엔티티를 DTO로 변환하는 생성자
    public ResponsePaymentDTO(Payment payment) {
        this.paymentId = payment.getId();
        this.orderId = payment.getOrder().getId();
        this.amount = payment.getAmount();               // 금액 추가
        this.status = payment.getStatus().name();        // Enum을 String으로 변환
        this.createdAt = payment.getCreatedAt();         // 생성 시간 추가
    }
}
