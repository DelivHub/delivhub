package com.sparta.delivhub.domain.order.service.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderType {
    ONLINE("온라인 주문");

    private final String description;
}

/*1. 요구사항 명시적 반영  주문 유형 : 온라인 주문만 가능하다고 했다.
문자열 이 아닌 ENUM  으로 정의를 하고 이시스템은 주문의 형태를 구분해서 관리 하겠다 .

왜 ENUM 으로 관리를 할까 만약 string 이나 boolean으로 작성을 하면 나중에 기능이 추가가 되면 DB스키마 변경과 함께
비즈니스 로직을 다 고쳐야 되기 때문에 Order Type (주문 유형)을 Enum 타입을 써두면 나중에 기능을 추가하면 기존 코드
건들지 않고 기능을 확장할 수 있기 때문에 ENUM 채택.
* */