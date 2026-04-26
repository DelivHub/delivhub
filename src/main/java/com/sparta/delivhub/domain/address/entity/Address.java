package com.sparta.delivhub.domain.address.entity;

import com.sparta.delivhub.common.entity.BaseEntity;
import com.sparta.delivhub.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "p_address")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Address extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 50)
    private String alias;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String detail;

    @Column(nullable = false, length = 10)
    private String zipCode;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    public void updateAddress(String alias, String address, String detail, String zipCode) {
        if (alias != null) {
            this.alias = alias;
        }

        if (address != null) {
            this.address = address;
        }

        if (detail != null) {
            this.detail = detail;
        }

        if (zipCode != null) {
            this.zipCode = zipCode;
        }
    }

    public void changeDefault(Boolean isDefault) {
        if (isDefault != null) {
            this.isDefault = isDefault;
        }
    }
}
