package com.sparta.delivhub.domain.review.entity;

import com.sparta.delivhub.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "p_reviews") // 명세서 도메인에 맞춰 테이블명 지정
@Getter
@NoArgsConstructor
public class Review extends BaseEntity { // 우리가 만든 BaseEntity 상속!

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // 명세서 요구사항인 UUID 적용
    private UUID id;

    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String content;

    private UUID orderId;
    private UUID storeId;
}