package com.sparta.tdd.domain.cart.repository;

import com.sparta.tdd.domain.cart.entity.Cart;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId AND c.deletedAt IS NULL")
    Optional<Cart> findByUserIdAndNotDeleted(@Param("userId") Long userId);

    @Query("""
        SELECT DISTINCT c FROM Cart c
        LEFT JOIN FETCH c.cartItems ci
        LEFT JOIN FETCH ci.menu m
        LEFT JOIN FETCH m.store s
        LEFT JOIN FETCH c.store cs
        WHERE c.user.id = :userId 
        AND c.deletedAt IS NULL
        """)
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);

}