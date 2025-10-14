package com.sparta.tdd.domain.store.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.tdd.domain.menu.entity.QMenu;
import com.sparta.tdd.domain.order.entity.QOrder;
import com.sparta.tdd.domain.store.entity.QStore;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.domain.user.entity.QUser;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<UUID> findPagedStoreIdsByKeyword(Pageable pageable, String keyword,
        StoreCategory storeCategory) {

        QStore store = QStore.store;
        QMenu menu = QMenu.menu;
        QOrder order = QOrder.order;

        return queryFactory
            .select(store.id)
            .from(store)
            .leftJoin(menu).on(menu.store.eq(store))
            .where(
                storeIsNotDeleted(),
                menuIsNotHidden(),
                storeCategoryEq(storeCategory),
                storeNameLike(keyword)
                    .or(menuNameLike(keyword))
            )
            .groupBy(store.id)
            .orderBy(toOrderSpecifiers(pageable, store, order))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    @Override
    public List<Tuple> findStoresWithMenusByIds(List<UUID> storeIds) {
        QStore store = QStore.store;
        QUser user = QUser.user;
        QMenu menu = QMenu.menu;

        return queryFactory
            .select(store, menu)
            .from(store)
            .leftJoin(store.user, user).fetchJoin()
            .leftJoin(menu).on(menu.store.eq(store)).fetchJoin()
            .where(store.id.in(storeIds))
            .fetch();
    }

    @Override
    public Long countStoresByKeyword(String keyword, StoreCategory storeCategory) {
        QStore store = QStore.store;
        QMenu menu = QMenu.menu;

        return queryFactory
            .select(store.countDistinct())
            .from(store)
            .leftJoin(menu).on(menu.store.eq(store))
            .where(
                storeCategoryEq(storeCategory),
                storeNameLike(keyword).or(menuNameLike(keyword))
            )
            .fetchOne();
    }

    private BooleanExpression storeIsNotDeleted() {
        QStore store = QStore.store;
        return store.deletedAt.isNull();
    }

    private BooleanExpression menuIsNotHidden() {
        QMenu menu = QMenu.menu;
        return menu.isHidden.isFalse();
    }


    private BooleanExpression storeNameLike(String keyword) {
        QStore store = QStore.store;
        return keyword != null ? store.name.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression menuNameLike(String keyword) {
        QMenu menu = QMenu.menu;
        return keyword != null ? menu.name.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression storeCategoryEq(StoreCategory storeCategory) {
        QStore store = QStore.store;
        return storeCategory != null ? store.category.eq(storeCategory) : null;
    }

    private OrderSpecifier<?>[] toOrderSpecifiers(Pageable pageable, QStore store, QOrder order) {
        Sort sort = pageable.getSort();

        if (sort.isUnsorted()) {
            return new OrderSpecifier[]{store.avgRating.desc()};
        }

        List<OrderSpecifier<?>> orders = new ArrayList<>();

        for (Sort.Order sortOrder : sort) {
            boolean asc = sortOrder.isAscending();
            String property = sortOrder.getProperty();

            switch (property) {
                case "avgRating" ->
                    orders.add(asc ? store.avgRating.asc() : store.avgRating.desc());
                case "reviewCount" ->
                    orders.add(asc ? store.reviewCount.asc() : store.reviewCount.desc());
                case "createdAt" ->
                    orders.add(asc ? store.createdAt.asc() : store.createdAt.desc());
                case "orderCount" -> {
                    Expression<Long> orderCountExpr = JPAExpressions
                        .select(order.count())
                        .from(order)
                        .where(order.store.id.eq(store.id));
                    orders.add(new OrderSpecifier<>(asc ? Order.ASC : Order.DESC, orderCountExpr));
                }
                default -> store.avgRating.desc();
            }
        }

        return orders.toArray(new OrderSpecifier[0]);
    }
}
