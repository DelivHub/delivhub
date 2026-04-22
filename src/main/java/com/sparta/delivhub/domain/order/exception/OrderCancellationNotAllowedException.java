package com.sparta.delivhub.domain.order.exception;

public class OrderCancellationNotAllowedException extends RuntimeException {

    public OrderCancellationNotAllowedException() {
        super("주문 생성 후 5분이 경과하여 취소할 수 없습니다.");
    }

    public OrderCancellationNotAllowedException(String message) {
        super(message);
    }
}
