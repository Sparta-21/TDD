package com.sparta.tdd.domain.cart.entity;

import com.sparta.tdd.domain.cart.dto.request.CartItemRequestDto;
import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
import com.sparta.tdd.global.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "p_cart_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cart_item_id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer price;

    @Builder
    private CartItem(Cart cart, Menu menu, Integer quantity, Integer price) {
        this.cart = cart;
        this.menu = menu;
        this.quantity = quantity;
        this.price = price;
    }


    public static CartItem of(Menu menu, CartItemRequestDto request) {
        return CartItem.builder()
                .menu(menu)
                .quantity(request.quantity())
                .price(menu.getPrice())
                .build();
    }

    public void assignCart(Cart cart) {
        this.cart = cart;
    }

    public void updateQuantity(Integer quantity) {
        if(quantity <= 0) {
            throw new BusinessException(ErrorCode.CART_ITEM_INVALID_QUANTITY);
        }
        this.quantity = quantity;
    }

    public Store getStore() {
        return this.menu.getStore();
    }
}