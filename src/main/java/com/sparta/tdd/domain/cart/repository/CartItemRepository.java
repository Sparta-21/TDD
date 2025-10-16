package com.sparta.tdd.domain.cart.repository;

import com.sparta.tdd.domain.cart.entity.CartItem;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.menu.id = :menuId AND ci.deletedAt IS NULL")
    Optional<CartItem> findByCartIdAndMenuId(@Param("cartId") UUID cartId, @Param("menuId") UUID menuId);

}