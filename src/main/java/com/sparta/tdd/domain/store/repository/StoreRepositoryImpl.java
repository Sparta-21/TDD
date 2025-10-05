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

    private BooleanBuilder buildStoreFilter(String keyword, StoreCategory storeCategory,
        boolean includeStoreName) {
        QStore store = QStore.store;
        BooleanBuilder builder = new BooleanBuilder();

        if (includeStoreName && keyword != null && !keyword.isBlank()) {
            builder.and(store.name.containsIgnoreCase(keyword));
        }

        if (storeCategory != null) {
            builder.and(store.category.eq(storeCategory));
        }

        return builder;
    }
}
