package com.sparta.delivhub.domain.option.entity;

import com.sparta.delivhub.common.entity.BaseEntity;
import com.sparta.delivhub.domain.menu.entity.Menu;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
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

    // 옵션 그룹명: 예) 맵기 선택, 추가 선택, 사이즈 선택
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // SINGLE = 1개 선택, MULTIPLE = 여러 개 선택
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private OptionType type;

    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionItem> optionItems = new ArrayList<>();

    @Builder
    public Option(Menu menu, String name, OptionType type) {
        this.menu = menu;
        this.name = name;
        this.type = type;
    }

    public void update(String name, OptionType type) {
        if (name != null) {
            this.name = name;
        }

        if (type != null) {
            this.type = type;
        }
    }

    public void assignMenu(Menu menu) {
        this.menu = menu;
    }

    public void addOptionItem(OptionItem optionItem) {
        this.optionItems.add(optionItem);
        optionItem.assignOption(this);
    }

    public void clearOptionItems() {
        this.optionItems.clear();
    }
}