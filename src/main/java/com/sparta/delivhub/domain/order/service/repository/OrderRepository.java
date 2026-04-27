package com.sparta.delivhub.domain.order.service.repository;

import com.sparta.delivhub.domain.order.service.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findById(UUID id);

    Page<Order> findAllByUserId(String userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.storeId IN :storeIds")
    Page<Order> findAllByStoreIdIn(@Param("storeIds") Iterable<UUID> storeIds, Pageable pageable);

    Page<Order> findAll(Pageable pageable);
}
