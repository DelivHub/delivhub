package com.sparta.delivhub.domain.payment.entity;

import com.sparta.delivhub.common.entity.BaseEntity;
import com.sparta.delivhub.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import java.util.UUID;

@Entity
@Table(name = "p_payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 조회 시 삭제되지 않은(deleted_at IS NULL) 데이터만 가져옴
@SQLRestriction("deleted_at IS NULL")
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // 결제와 주문은 1:1 관계 (ERD 상 order_id가 UNIQUE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Builder
    public Payment(Order order, Integer amount, PaymentMethod paymentMethod, PaymentStatus status) {
        this.order = order;
        this.amount = amount;
        this.paymentMethod = (paymentMethod != null) ? paymentMethod : PaymentMethod.CARD;
        this.status = (status != null) ? status : PaymentStatus.PENDING;
    }

    // 결제 상태 변경
    public void updateStatus(PaymentStatus status) {
        this.status = status;
    }

    // (선택) BaseEntity의 softDelete를 활용하여 결제 취소/삭제 로직을 한 번에 처리
    public void cancelPayment(String deletedBy) {
        this.status = PaymentStatus.CANCELLED; // 혹은 DELETED
        super.softDelete(deletedBy);           // BaseEntity의 삭제 시간, 삭제자 기록
    }
}