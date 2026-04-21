package com.sparta.delivhub.domain.payment.entity;

public enum PaymentStatus {
    PENDING,    // 결제 승인 대기 중 (최초 주문 시 기본값)
    COMPLETED,  // 결제 완료
    CANCELLED,  // 결제 취소됨 (사용자 또는 관리자가 취소)
    DELETED     // 결제 내역 삭제됨 (소프트 삭제용)
}