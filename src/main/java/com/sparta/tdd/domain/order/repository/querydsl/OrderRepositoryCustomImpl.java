package com.sparta.tdd.domain.order.repository.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.tdd.domain.menu.entity.QMenu;
import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.entity.QOrder;
import com.sparta.tdd.domain.order.repository.OrderRepositoryCustom;
import com.sparta.tdd.domain.orderMenu.entity.QOrderMenu;
import com.sparta.tdd.domain.payment.entity.QPayment;
import com.sparta.tdd.domain.store.entity.QStore;
import com.sparta.tdd.domain.user.entity.QUser;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public Optional<Order> findDetailById(UUID id) {
        QOrder o = QOrder.order;
        QUser u = QUser.user;
        QStore s = QStore.store;
        QPayment p = QPayment.payment;
        QOrderMenu om = QOrderMenu.orderMenu;
        QMenu m = QMenu.menu;

        Order result = query
            .selectFrom(o)
            .distinct()
            .leftJoin(o.user, u).fetchJoin()
            .leftJoin(o.store, s).fetchJoin()
            .leftJoin(o.payment, p).fetchJoin()
            .leftJoin(o.orderMenuList, om).fetchJoin()
             .leftJoin(om.menu, m).fetchJoin()
            .where(o.id.eq(id))
            .fetchOne();

        return Optional.ofNullable(result);
    }
}
