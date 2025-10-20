package com.sparta.tdd.domain.order.repository.querydsl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.tdd.domain.menu.entity.QMenu;
import com.sparta.tdd.domain.order.dto.OrderSearchOptionDto;
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
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public Optional<Order> findDetailById(UUID id) {
        QOrder qOrder = QOrder.order;
        QUser qOrderUser = new QUser("orderUser");
        QUser qStoreUser = new QUser("storeUser");
        QStore qStore = QStore.store;
        QPayment qPayment = QPayment.payment;
        QOrderMenu qOrderMenu = QOrderMenu.orderMenu;
        QMenu qMenu = QMenu.menu;

        Order result = query
            .selectFrom(qOrder)
            .distinct()
            .leftJoin(qOrder.user, qOrderUser).fetchJoin()
            .leftJoin(qOrder.store, qStore).fetchJoin()
            .leftJoin(qOrder.store.user, qStoreUser).fetchJoin()
            .leftJoin(qOrder.payment, qPayment).fetchJoin()
            .leftJoin(qOrder.orderMenuList, qOrderMenu).fetchJoin()
            .leftJoin(qOrderMenu.menu, qMenu).fetchJoin()
            .where(qOrder.id.eq(id))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    /**
     * PageableExecutionUtils 도 있습니다 참고해주세요!(count 쿼리 성능 개선 부분)
     * <a href = https://junior-datalist.tistory.com/342>참고주소</a>
     */
    @Override
    public Page<UUID> findPageIds(
        Pageable pageable,
        OrderSearchOptionDto searchOption) {


        Long targetUserId = searchOption.userId();
        LocalDateTime start = searchOption.startOrNull();
        LocalDateTime end = searchOption.endOrNull();
        UUID targetStoreId = searchOption.storeId();


        QOrder qOrder = QOrder.order;

        List<UUID> ids = query
            .select(qOrder.id)
            .from(qOrder)
            .where(
                userIdEq(targetUserId),
                storeIdEq(targetStoreId),
                createdAtGoe(start),
                createdAtLt(end)
            )
            .orderBy(toOrderSpecifier(pageable.getSort(), qOrder))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = query
                .select(qOrder.count())
                .from(qOrder)
                .where(
                    userIdEq(targetUserId),
                    storeIdEq(targetStoreId),
                    createdAtGoe(start),
                    createdAtLt(end)
                );

        return PageableExecutionUtils.getPage(
                ids,
                pageable,
                countQuery::fetchOne
        );
    }


    private OrderSpecifier<?>[] toOrderSpecifier(Sort sort, QOrder o) {
        if (sort == null || sort.isUnsorted()) {
            return new OrderSpecifier[]{ o.createdAt.desc() };
        }

        List<OrderSpecifier<?>> specifiers = new ArrayList<>();
        for (Sort.Order sortOrder : sort) {

            com.querydsl.core.types.Order direction =
                sortOrder.isAscending() ?
                    com.querydsl.core.types.Order.ASC : com.querydsl.core.types.Order.DESC;


            switch (sortOrder.getProperty()) {
                case "createdAt" -> specifiers.add(new OrderSpecifier<>(direction, o.createdAt));
                case "id" -> specifiers.add(new OrderSpecifier<>(direction, o.id));
                case "orderStatus" -> specifiers.add(new OrderSpecifier<>(direction, o.orderStatus));
                default -> {

                }
            }
        }

        return specifiers.toArray(new OrderSpecifier[0]);
    }

    private BooleanExpression userIdEq(Long id) {
        return id == null ? null : QOrder.order.user.id.eq(id);
    }
    private BooleanExpression storeIdEq(UUID id) {
        return id == null ? null : QOrder.order.store.id.eq(id);
    }
    private BooleanExpression createdAtGoe(LocalDateTime t) {
        return t == null ? null : QOrder.order.createdAt.goe(t);
    }
    private BooleanExpression createdAtLt(LocalDateTime t) {
        return t == null ? null : QOrder.order.createdAt.lt(t);
    }
}
