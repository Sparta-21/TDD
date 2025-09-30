package com.sparta.tdd.domain.orderMenu.entity;

import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.global.model.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.awt.Menu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "p_order_menu")
public class OrderMenu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private Long id;

    private Integer quantity;

    private Integer price;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;
}
