package com.sparta.delivhub.domain.order.service.exception;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;

public class OrderCancellationNotAllowedException extends BusinessException {
    public OrderCancellationNotAllowedException() {
        super(ErrorCode.ORDER_CANCEL_TIMEOUT);
    }
}
