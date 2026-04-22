package com.sparta.delivhub.domain.order.repository;

import com.sparta.delivhub.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByIdAndDeletedAtIsNull(UUID id);

    Page<Order> findAllByUserIdAndDeletedAtIsNull(String userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.storeId IN :storeIds AND o.deletedAt IS NULL")
    Page<Order> findAllByStoreIdInAndDeletedAtIsNull(@Param("storeIds") Iterable<UUID> storeIds, Pageable pageable);

    Page<Order> findAllByDeletedAtIsNull(Pageable pageable);
}
