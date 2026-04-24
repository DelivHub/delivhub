package com.sparta.delivhub.domain.payment.dto;

import com.sparta.delivhub.domain.payment.entity.Payment;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class StorePaymentListResponseDto {
    private List<StorePaymentDto> content;

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private String sort;

    public StorePaymentListResponseDto(Page<Payment> paymentPage) {
        this.content = paymentPage.getContent().stream()
                .map(StorePaymentDto::new)
                .collect(Collectors.toList());

        this.page = paymentPage.getNumber();
        this.size = paymentPage.getSize();
        this.totalElements = paymentPage.getTotalElements();
        this.totalPages = paymentPage.getTotalPages();
        this.sort = paymentPage.getSort().toString();
    }
}