package com.sparta.delivhub.domain.order.repository;

import com.sparta.delivhub.domain.order.Entity.Order;
import com.sparta.delivhub.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
