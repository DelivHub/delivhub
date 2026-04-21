package com.sparta.delivhub.domain.review.entity;

import com.sparta.delivhub.common.entity.BaseEntity;
import com.sparta.delivhub.domain.order.Entity.Order;
import com.sparta.delivhub.domain.store.Entity.Store;
import com.sparta.delivhub.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "p_review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // 하나의 주문에 하나의 리뷰만 작성 가능 (UNIQUE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    // 여러 리뷰가 하나의 가게에 속함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // 여러 리뷰가 한 명의 유저에 의해 작성됨
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 255)
    private String content;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Builder
    public Review(Order order, Store store, User user, Integer rating, String content, String imageUrl) {
        this.order = order;
        this.store = store;
        this.user = user;
        this.rating = rating;
        this.content = content;
        this.imageUrl = imageUrl;
    }
}