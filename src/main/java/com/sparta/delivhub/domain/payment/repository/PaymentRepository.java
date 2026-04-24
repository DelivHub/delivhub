package com.sparta.delivhub.domain.payment.repository;

import com.sparta.delivhub.domain.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    // 이미 이 주문으로 결제된 내역이 있는지 확인 (반환값 boolean)
    boolean existsByOrderId(UUID orderId);

    //이미 삭제된 데이터까지 포함해서 조회
    @Query("SELECT p FROM Payment p WHERE p.id = :id")
    Optional<Payment> findByIdWithDeleted(@Param("id") UUID id);

    // 특정 유저의 결제 내역 목록을 페이징하여 조회
    Page<Payment> findAllByUserId(String userId, Pageable pageable);

    // 주문(Order) 엔티티의 storeId를 기준으로 결제 내역을 페이징하여 조회
    @Query("SELECT p FROM Payment p WHERE p.order.storeId = :storeId")
    Page<Payment> findAllByStoreId(@Param("storeId") UUID storeId, Pageable pageable);

    Optional<Payment> findByOrderId(UUID orderId);
}
