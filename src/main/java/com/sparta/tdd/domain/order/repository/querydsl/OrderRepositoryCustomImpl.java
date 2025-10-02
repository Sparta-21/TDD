package com.sparta.tdd.domain.order.repository.querydsl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.tdd.domain.menu.entity.QMenu;
import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.entity.QOrder;
import com.sparta.tdd.domain.order.repository.OrderRepositoryCustom;
import com.sparta.tdd.domain.orderMenu.entity.QOrderMenu;
import com.sparta.tdd.domain.payment.entity.QPayment;
import com.sparta.tdd.domain.store.entity.QStore;
import com.sparta.tdd.domain.user.entity.QUser;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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

    @Override
    public Page<UUID> findPageIds(
        Pageable pageable,
        Long targetUserId,
        LocalDateTime start,
        LocalDateTime end,
        UUID targetStoreId) {

        QOrder o = QOrder.order;

        List<UUID> ids = query
            .select(o.id)
            .from(o)
            .where(
                o.user.id.eq(targetUserId),
                o.store.id.eq(targetStoreId),
                o.createdAt.goe(start),
                o.createdAt.lt(end)
            )
            .orderBy(toOrderSpecifier(pageable.getSort(), o))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = query
            .select(o.count())
            .from(o)
            .where(
                o.user.id.eq(targetUserId),
                o.store.id.eq(targetStoreId),
                o.createdAt.goe(start),
                o.createdAt.lt(end)
            )
            .fetchOne();

        if (total == null) {
            total = 0L;
        }

        return new PageImpl<>(ids, pageable, total);
    }

    private OrderSpecifier<?>[] toOrderSpecifier(Sort sort, QOrder o) {
        if (sort == null || sort.isUnsorted()) {
            return new OrderSpecifier[]{ o.createdAt.desc() };
        }

        List<OrderSpecifier<?>> specifiers = new ArrayList<>();
        for (Sort.Order sort_order : sort) {

            com.querydsl.core.types.Order direction =
                sort_order.isAscending() ?
                    com.querydsl.core.types.Order.ASC : com.querydsl.core.types.Order.DESC;


            switch (sort_order.getProperty()) {
                case "createdAt" -> specifiers.add(new OrderSpecifier<>(direction, o.createdAt));
                case "id" -> specifiers.add(new OrderSpecifier<>(direction, o.id));
                case "orderStatus" -> specifiers.add(new OrderSpecifier<>(direction, o.orderStatus));
                default -> {

                }
            }
        }

        return specifiers.toArray(new OrderSpecifier[0]);
    }


}
