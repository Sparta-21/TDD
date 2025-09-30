package com.sparta.tdd.domain.orderMenu.repository;

import com.sparta.tdd.domain.orderMenu.entity.OrderMenu;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderMenuRepository extends JpaRepository<OrderMenu, UUID> {

}
