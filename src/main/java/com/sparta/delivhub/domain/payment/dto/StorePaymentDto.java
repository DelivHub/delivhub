package com.sparta.delivhub.domain.payment.dto;

import com.sparta.delivhub.domain.payment.entity.Payment;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class StorePaymentDto {
    private UUID paymentId;
    private UUID orderId; // 명세서 요구사항: 주문 ID 포함
    private Long amount;
    private String status;
    private LocalDateTime createdAt;

    public StorePaymentDto(Payment payment) {
        this.paymentId = payment.getId();
        // Payment 엔티티가 Order 객체를 연관관계로 가지고 있다고 가정합니다.
        this.orderId = payment.getOrder().getId();
        this.amount = payment.getAmount();
        this.status = payment.getStatus().name();
        this.createdAt = payment.getCreatedAt();
    }
}