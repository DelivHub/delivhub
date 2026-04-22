package com.sparta.delivhub.domain.order.exception;

public class UnauthorizedOrderAccessException extends RuntimeException {

    public UnauthorizedOrderAccessException() {
        super("해당 주문에 대한 접근 권한이 없습니다.");
    }

    public UnauthorizedOrderAccessException(String message) {
        super(message);
    }
}
