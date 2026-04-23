package com.sparta.delivhub.domain.review.repository;

import com.sparta.delivhub.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // 주문 아이디로 중복 리뷰 존재 여부 확인 (이전에 작성한 것)
    boolean existsByOrderId(UUID orderId);

    //  특정 유저의 리뷰 목록을 페이징하여 조회
    Page<Review> findAllByUserId(String userId, Pageable pageable);
}