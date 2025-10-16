package com.sparta.tdd.domain.cart.entity;

import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
import com.sparta.tdd.global.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "p_cart")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cart_id", nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    @Builder
    public Cart(User user) {
        this.user = user;
    }

    // 장바구니 아이템 추가
    public void addCartItem(CartItem cartItem, Store store) {
        if (this.store != null && !this.store.getId().equals(store.getId())) {
            throw new BusinessException(ErrorCode.CART_DIFFERENT_STORE);
        }

        // 장바구니가 비어있으면 가게 설정
        if (this.store == null) {
            this.store = store;
        }

        this.cartItems.add(cartItem);
        cartItem.assignCart(this);
    }

    // 장바구니 비우기
    public void clearCart() {
        this.cartItems.clear();
        this.store = null;
    }

    // 장바구니에서 호출
    // 비게 되면 가게 정보도 삭제
    public void checkAndClearStoreIfEmpty() {
        if (this.cartItems.isEmpty() ||
                this.cartItems.stream().allMatch(BaseEntity::isDeleted)) {
            this.store = null;
        }
    }
}