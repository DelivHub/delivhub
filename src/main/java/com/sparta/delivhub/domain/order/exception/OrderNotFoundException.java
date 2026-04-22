package com.sparta.delivhub.domain.order.exception;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(UUID orderId) {
        super("존재하지 않는 주문입니다. ID: " + orderId);
    }

    public OrderNotFoundException(String message) {
        super(message);
    }
}
