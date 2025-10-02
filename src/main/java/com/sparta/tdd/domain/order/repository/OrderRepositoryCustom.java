package com.sparta.tdd.domain.order.repository;

import com.sparta.tdd.domain.order.entity.Order;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepositoryCustom {
    Optional<Order> findDetailById(UUID id);
}
