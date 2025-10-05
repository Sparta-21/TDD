package com.sparta.tdd.domain.store.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.tdd.domain.menu.entity.QMenu;
import com.sparta.tdd.domain.store.dto.StoreResponseDto;
import com.sparta.tdd.domain.store.entity.QStore;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.domain.user.entity.QUser;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<UUID> findStoreIdsByMenuKeyword(String keyword, StoreCategory storeCategory) {
        QStore store = new QStore("store");
        QMenu menu = new QMenu("menu");

        BooleanBuilder builder = buildStoreFilter(keyword, storeCategory, false);

        return queryFactory
            .select(store.id)
            .from(menu)
            .join(menu.store, store)
            .where(builder)
            .fetch();
    }

    @Override
    public List<UUID> findStoreIdsByStoreNameKeyword(String keyword, StoreCategory storeCategory) {
        QStore store = QStore.store;

        BooleanBuilder builder = buildStoreFilter(keyword, storeCategory, true);

        return queryFactory
            .select(store.id)
            .from(store)
            .where(builder)
            .fetch();
    }

    @Override
    public Page<StoreResponseDto> findStoresByIds(List<UUID> storeIds, Pageable pageable) {
        QStore store = QStore.store;
        QUser user = QUser.user;

        List<StoreResponseDto> stores = queryFactory
            .select(StoreResponseDto.qConstructor(store))
            .from(store)
            .leftJoin(store.user, user)
            .where(store.id.in(storeIds))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(store.count())
            .from(store)
            .where(store.id.in(storeIds));

        return PageableExecutionUtils.getPage(stores, pageable, countQuery::fetchOne);
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

    private BooleanExpression storeIsNotDeleted() {
        QStore store = QStore.store;
        return store.deletedAt.isNull();
    }

    private BooleanExpression menuIsNotHidden() {
        QMenu menu = QMenu.menu;
        return menu.isHidden.isFalse();
    }
}
