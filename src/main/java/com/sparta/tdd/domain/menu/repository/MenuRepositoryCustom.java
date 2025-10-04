package com.sparta.tdd.domain.menu.repository;

import com.sparta.tdd.domain.menu.dto.MenuWithStoreResponseDto;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MenuRepositoryCustom {

    Map<UUID, List<MenuWithStoreResponseDto>> findByStoreIds(List<UUID> storeIds);
}
