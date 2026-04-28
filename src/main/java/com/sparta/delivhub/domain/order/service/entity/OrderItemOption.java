package com.sparta.delivhub.domain.order.service.entity;

import com.sparta.delivhub.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "p_order_item_option")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemOption extends BaseEntity {
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(name = "option_id", nullable = false)
    private UUID optionId;

    @Column(name = "option_name", nullable = false, length = 100)
    private String optionName;

    @Column(name = "option_item_id", nullable = false)
    private UUID optionItemId;

    @Column(name = "option_item_name", nullable = false, length = 100)
    private String optionItemName;

    @Column(name = "extra_price", nullable = false)
    private Long extraPrice;

    @Builder
    public OrderItemOption(
            UUID optionId,
            String optionName,
            UUID optionItemId,
            String optionItemName,
            Long extraPrice
    ) {
        this.optionId = optionId;
        this.optionName = optionName;
        this.optionItemId = optionItemId;
        this.optionItemName = optionItemName;
        this.extraPrice = extraPrice != null ? extraPrice : 0L;
    }

    protected void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
    }
}