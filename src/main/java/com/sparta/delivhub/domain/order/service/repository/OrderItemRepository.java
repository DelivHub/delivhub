package com.sparta.delivhub.domain.order.service.repository;

import com.sparta.delivhub.domain.order.service.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
}
