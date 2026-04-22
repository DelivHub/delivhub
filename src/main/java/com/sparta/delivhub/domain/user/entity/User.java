package com.sparta.delivhub.domain.user.entity;

import com.sparta.delivhub.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "p_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @Column(name = "username", nullable = false, length = 10)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role",nullable = false, length = 20)
    private UserRole userRole;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = true;
}
