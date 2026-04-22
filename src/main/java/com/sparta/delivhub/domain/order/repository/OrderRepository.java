package com.sparta.delivhub.domain.order.repository;

import com.sparta.delivhub.domain.order.entity.Order;
<<<<<<< HEAD
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    // Soft Delete된 데이터는 조회되지 않도록 처리
    @Query("SELECT o FROM Order o WHERE o.id = :id AND o.deletedAt IS NULL")
    Optional<Order> findByIdAndDeletedAtIsNull(@Param("id") UUID id);

    // CUSTOMER용: 본인 주문만 조회
    Page<Order> findAllByUserIdAndDeletedAtIsNull(String userId, Pageable pageable);

    // OWNER용: 본인 가게의 주문만 조회
    @Query("SELECT o FROM Order o WHERE o.storeId IN :storeIds AND o.deletedAt IS NULL")
    Page<Order> findAllByStoreIdInAndDeletedAtIsNull(@Param("storeIds") Iterable<UUID> storeIds, Pageable pageable);

    // MANAGER/MASTER용: 전체 조회
    Page<Order> findAllByDeletedAtIsNull(Pageable pageable);
=======
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
>>>>>>> develop
}
