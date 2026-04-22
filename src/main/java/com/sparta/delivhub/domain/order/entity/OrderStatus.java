package com.sparta.delivhub.domain.order.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/// ENUM (열거형)은 주문 시스템의 상태 전이 규칙을 안전하고 명확하게 관리하기 위해서 ENUM 을 사용
@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PENDING("주문요청"),
    ACCEPTED("주문수락"),
    COOKING("조리중"),
    DELIVERING("배송중"),
    DELIVERED("배송완료"),
    COMPLETED("주문완료"),
    CANCELED("주문취소");

    private final String description;


     /// 주문상태  (역방향 불가 로직)
     /// 실수방지 : 점주가 실수로 '배송 완료' 버튼을 눌렀다가 다시 '조리 중 '으로 바꾸는 것을 시스템적으로 막음

    public boolean canTransitionTo(OrderStatus nextStatus) {/// 종료된 상태이면 어떤 상태로 변경 불가능
        if (this == CANCELED || this == COMPLETED) {
            return false;
        }
        return nextStatus.ordinal() > this.ordinal();///상태는 순서대로만 역방향 불가, "다음 상태의 숫자가 현재보다 커야 한다

        /// 비즈니스 규칙 : 상태는 반드시 정해진 순서대로만 이동해야함 (주문로직이기 때문에 )
        /// ordinal()은 선언된 순서(0, 1, 2...)를 숫자값으로 반환한다.
        /// PENDING(0) < ACCEPTED(1) < COOKING(2) 예시

    }
}
