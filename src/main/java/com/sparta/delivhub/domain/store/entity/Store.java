package com.sparta.delivhub.domain.store.entity;


import com.sparta.delivhub.domain.area.entity.Area;
import com.sparta.delivhub.domain.category.entity.Category;
import com.sparta.delivhub.common.entity.BaseEntity;
import com.sparta.delivhub.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "p_store")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE p_store SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL AND is_hidden = false")
public class Store extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "number", nullable = false)
    private String number;

    @Builder.Default
    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = true;

    @Builder.Default
    @Column(name = "average_rating", nullable = false, precision = 2, scale = 1)
    private BigDecimal averageRating = BigDecimal.ZERO;

    public void updateStore(String name, String address, Boolean isHidden, Category category, Area area, String number) {
        this.name = name;
        this.address = address;
        this.category = category;
        this.area = area;
        this.number = number;
        this.isHidden = isHidden;
    }

    // 평균 별점 업데이트 메서드
    public void updateAverageRating(BigDecimal newAverageRating) {
        this.averageRating = newAverageRating;
    }
}