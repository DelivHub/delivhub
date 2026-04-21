package com.sparta.delivhub.domain.option.entity;

import com.sparta.delivhub.common.entity.BaseEntity;
import com.sparta.delivhub.domain.menu.entity.Menu;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@Entity
@Table(name = "p_option")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Option extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "extra_price", nullable = false)
    private Long extraPrice = 0L;

    @Builder
    public Option(Menu menu, String name, Long extraPrice) {
        this.menu = menu;
        this.name = name;
        this.extraPrice = extraPrice != null ? extraPrice : 0L;
    }

    public void update(String name, Long extraPrice) {
        if (name != null) this.name = name;
        if (extraPrice != null) this.extraPrice = extraPrice;
    }
}