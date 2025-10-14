package com.sparta.tdd.domain.store.repository;

import com.querydsl.core.Tuple;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface StoreRepositoryCustom {

    List<UUID> findPagedStoreIdsByKeyword(Pageable pageable, String keyword,
        StoreCategory storeCategory);

    List<Tuple> findStoresWithMenusByIds(List<UUID> storeIds);

    Long countStoresByKeyword(String keyword, StoreCategory storeCategory);
}
