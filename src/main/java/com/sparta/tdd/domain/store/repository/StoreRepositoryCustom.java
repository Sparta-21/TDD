package com.sparta.tdd.domain.store.repository;

import com.sparta.tdd.domain.store.dto.StoreResponseDto;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StoreRepositoryCustom {

    List<UUID> findStoreIdsByMenuKeyword(String keyword, StoreCategory storeCategory);

    List<UUID> findStoreIdsByStoreNameKeyword(String keyword, StoreCategory storeCategory);

    Page<StoreResponseDto> findStoresByIds(List<UUID> storeIds, Pageable pageable);
}
