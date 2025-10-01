package com.sparta.tdd.domain.orderMenu.entity;

import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.global.model.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "p_order_menu")
public class OrderMenu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private Integer quantity;

    private Integer price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Builder
    public OrderMenu(Integer quantity, Integer price, Order order, Menu menu) {
        this.quantity = quantity;
        this.price = price;
        this.order = order;
        this.menu = menu;
    }

    public void assignOrder(Order order) {
        this.order = order;
    }

    public void assignMenu(Menu menu) {
        this.menu = menu;
    }

    public void updateQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
