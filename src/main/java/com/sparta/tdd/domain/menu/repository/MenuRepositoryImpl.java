package com.sparta.tdd.domain.menu.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.tdd.domain.menu.dto.MenuWithStoreResponseDto;
import com.sparta.tdd.domain.menu.entity.QMenu;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MenuRepositoryImpl implements MenuRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Map<UUID, List<MenuWithStoreResponseDto>> findByStoreIds(List<UUID> storeIds) {
        QMenu menu = QMenu.menu;

        List<MenuWithStoreResponseDto> menus = queryFactory
            .select(MenuWithStoreResponseDto.qConstructor(menu))
            .from(menu)
            .where(menu.store.id.in(storeIds))
            .fetch();

        return menus.stream()
            .collect(Collectors.groupingBy(MenuWithStoreResponseDto::storeId));
    }
}
