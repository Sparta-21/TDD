package com.sparta.tdd.domain.cart.repository;

import com.sparta.tdd.domain.cart.entity.Cart;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId AND c.deletedAt IS NULL")
    Optional<Cart> findByUserIdAndNotDeleted(@Param("userId") Long userId);

    @Query("""
    SELECT DISTINCT c FROM Cart c
    LEFT JOIN FETCH c.cartItems ci
    LEFT JOIN FETCH ci.menu m
    LEFT JOIN FETCH ci.store s
    WHERE c.user.id = :userId 
    AND c.deletedAt IS NULL
    """)
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);
}