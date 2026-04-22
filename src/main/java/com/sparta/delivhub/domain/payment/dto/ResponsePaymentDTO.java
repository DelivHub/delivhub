package com.sparta.delivhub.domain.payment.dto;

import com.sparta.delivhub.domain.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class 4ResponsePaymentDTO {
    private UUID paymentId;
    private UUID orderId;
    private Long amount;
    private String paymentMethod;
    private LocalDateTime createdAt;

    public ResponsePaymentDTO(Payment payment) {
        this.paymentId = payment.getId();
        this.orderId = payment.getOrderId();
        this.amount = payment.getAmount();
        this.paymentMethod = payment.getPaymentMethod().name();
        this.createdAt = payment.getCreatedAt();
    }
}
