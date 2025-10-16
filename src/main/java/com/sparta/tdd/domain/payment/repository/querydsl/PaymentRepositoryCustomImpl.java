package com.sparta.tdd.domain.payment.repository.querydsl;

import static com.sparta.tdd.domain.order.entity.QOrder.order;
import static com.sparta.tdd.domain.orderMenu.entity.QOrderMenu.orderMenu;
import static com.sparta.tdd.domain.payment.entity.QPayment.payment;
import static com.sparta.tdd.domain.store.entity.QStore.store;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.tdd.domain.payment.entity.Payment;
import com.sparta.tdd.domain.payment.enums.CardCompany;
import com.sparta.tdd.domain.payment.repository.PaymentRepositoryCustom;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class PaymentRepositoryCustomImpl implements PaymentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Payment> findPaymentsByUserId(Long userId, String keyword, Pageable pageable) {

        JPAQuery<Payment> query = queryFactory
            .selectFrom(payment)
            .leftJoin(payment.order, order).fetchJoin()
            .leftJoin(order.store, store).fetchJoin()
            .where(
                payment.user.id.eq(userId),
                payment.deletedAt.isNull(),
                keywordContains(keyword)
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(createOrderSpecifiers(pageable));

        List<Payment> content = query.fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(payment.count())
            .from(payment)
            .where(
                payment.user.id.eq(userId),
                payment.deletedAt.isNull(),
                keywordContains(keyword)
            );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<Payment> findPaymentsByStoreId(UUID storeId, String keyword, Pageable pageable) {
        JPAQuery<Payment> query = queryFactory
            .selectFrom(payment)
            .leftJoin(payment.order, order).fetchJoin()
            .leftJoin(order.store, store).fetchJoin()
            .where(
                store.id.eq(storeId),
                payment.deletedAt.isNull(),
                keywordContains(keyword)
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(createOrderSpecifiers(pageable));

        List<Payment> content = query.fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(payment.count())
            .from(payment)
            .leftJoin(payment.order, order)
            .leftJoin(order.store, store)
            .where(
                store.id.eq(storeId),
                payment.deletedAt.isNull(),
                keywordContains(keyword)
            );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Optional<Payment> findPaymentDetailById(UUID paymentId) {
        Payment result = queryFactory
            .selectFrom(payment)
            .leftJoin(payment.order, order).fetchJoin()
            .leftJoin(order.store, store).fetchJoin()
            .leftJoin(order.orderMenuList, orderMenu).fetchJoin()
            .where(
                payment.id.eq(paymentId),
                payment.deletedAt.isNull()
            )
            .fetchOne();

        return Optional.ofNullable(result);
    }

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        CardCompany cardCompany = CardCompany.findByName(keyword);
        return payment.cardCompany.eq(cardCompany);
    }

    private OrderSpecifier<?>[] createOrderSpecifiers(Pageable pageable) {
        if (pageable.getSort().isEmpty()) {
            return new OrderSpecifier<?>[]{payment.createdAt.desc()};
        }

        return pageable.getSort().stream()
            .map(this::getOrderSpecifier)
            .filter(Objects::nonNull)
            .toArray(OrderSpecifier[]::new);
    }

    private OrderSpecifier<?> getOrderSpecifier(org.springframework.data.domain.Sort.Order sortOrder) {
        String property = sortOrder.getProperty();
        boolean isAsc = sortOrder.isAscending();

        return switch (property) {
            case "amount" -> isAsc ? payment.amount.asc() : payment.amount.desc();
            case "createdAt" -> isAsc ? payment.createdAt.asc() : payment.createdAt.desc();
            default -> null;
        };
    }
}
