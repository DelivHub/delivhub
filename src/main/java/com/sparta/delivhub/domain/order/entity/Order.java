package com.sparta.delivhub.domain.order.entity;

import com.sparta.delivhub.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "p_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "address_id", nullable = false)
    private UUID addressId;

    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    @Column(name = "request")
    private String request;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Builder
    public Order(String userId, UUID storeId, UUID addressId, Long totalPrice, String request, OrderType orderType) {
        this.userId = userId;
        this.storeId = storeId;
        this.addressId = addressId;
        this.totalPrice = totalPrice;
        this.request = request;
        this.orderType = orderType;
        this.status = OrderStatus.PENDING;
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void updateRequest(String request) {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("접수된 주문은 요청사항을 수정할 수 없습니다.");
        }
        this.request = request;
    }

    ///  주문 상태를 다음 단계 접수 완료 -> 조리중
    public void updateStatus(OrderStatus nextStatus) {
        if (!this.status.canTransitionTo(nextStatus)) {
            throw new IllegalStateException("상태를 변경할 수 없습니다.");
        }
        this.status = nextStatus;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELED;
    }
}
