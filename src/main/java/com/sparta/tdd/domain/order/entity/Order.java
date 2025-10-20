package com.sparta.tdd.domain.order.entity;

import com.sparta.tdd.domain.order.enums.OrderStatus;
import com.sparta.tdd.domain.orderMenu.entity.OrderMenu;
import com.sparta.tdd.domain.payment.entity.Payment;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.global.model.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "p_order")
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderMenu> orderMenuList;

    @OneToOne(mappedBy = "order")
    private Payment payment;

    @Builder
    public Order(String address, OrderStatus orderStatus, List<OrderMenu> orderMenuList,
        Store store,
        User user) {
        this.address = address;
        this.orderStatus = (orderStatus != null) ? orderStatus : OrderStatus.PENDING;
        this.orderMenuList = orderMenuList != null ? orderMenuList : new ArrayList<>();
        this.store = store;
        this.user = user;
    }

    public void assignUser(User user) {
        this.user = user;
    }

    public void assignStore(Store store) {
        this.store = store;
    }

    public void addOrderMenu(OrderMenu orderMenu) {
        this.orderMenuList.add(orderMenu);
        orderMenu.assignOrder(this);
    }

    public void nextStatus() {
        this.orderStatus = this.orderStatus.next();
    }

    public void changeOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public boolean isOwnedBy(Long userId) {
        return this.getUser().getId().equals(userId);
    }

    public boolean canCancel() {
        Duration duration = Duration.between(super.getCreatedAt(), LocalDateTime.now());
        return duration.toMinutes() < 5;
    }
}
