package com.sparta.tdd.domain.order.repository;

import com.sparta.tdd.domain.order.dto.OrderSearchOptionDto;
import com.sparta.tdd.domain.order.entity.Order;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepositoryCustom {


    Optional<Order> findDetailById(UUID id);

    /**
     * 검색, 정렬과 페이징 조건에 맞는 orderId 를 반환합니다
     * @param pageable
     * @param targetUserId
     * @param start
     * @param end
     * @param targetStoreId
     * @return return new PageImpl<>(ids, pageable, total);
     */
    Page<UUID> findPageIds(
        Pageable pageable,
        OrderSearchOptionDto searchOption);

}
