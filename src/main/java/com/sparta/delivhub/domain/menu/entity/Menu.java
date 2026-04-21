package com.sparta.delivhub.domain.menu.entity;

import com.sparta.delivhub.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.sparta.delivhub.domain.store.entity.Store;

import java.util.UUID;

@Getter
@Entity
@Table(name = "p_menu")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_hidden", nullable = false)
    private boolean isHidden = false;

    @Builder
    public Menu(Store store, String name, Integer price, String description) {
        this.store = store;
        this.name = name;
        this.price = price;
        this.description = description;
        this.isHidden = false;
    }

    public void update(String name, Integer price, String description) {
        if (name != null) this.name = name;
        if (price != null) this.price = price;
        if (description != null) this.description = description;
    }

    public void updateHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }
}

