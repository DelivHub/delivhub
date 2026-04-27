package com.sparta.delivhub.domain.order.exception;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;

public class UnauthorizedOrderAccessException extends BusinessException {
    public UnauthorizedOrderAccessException() {
        super(ErrorCode.ORDER_ACCESS_DENIED_ON_READ);
    }

    public UnauthorizedOrderAccessException(String message) {
        super(ErrorCode.ORDER_ACCESS_DENIED_ON_READ);
    }
}
