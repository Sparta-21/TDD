package com.sparta.tdd.domain.menu.service;

import com.sparta.tdd.domain.menu.dto.MenuRequestDto;
import com.sparta.tdd.domain.menu.dto.MenuResponseDto;
import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.menu.repository.MenuRepository;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.repository.StoreRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuService {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private StoreRepository storeRepository;

    public List<MenuResponseDto> getMenus(UUID storeId) {
        List<Menu> menus = menuRepository.findAllByStoreId(storeId);
        return menus.stream()
            .map(MenuResponseDto::of)
            .collect(Collectors.toList());
    }

    public MenuResponseDto getMenu(UUID storeId, UUID menuId) {
        Menu menu = menuRepository.findByStoreIdAndId(storeId, menuId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴입니다."));
        return MenuResponseDto.of(menu);
    }

    @Transactional
    public MenuResponseDto createMenu(UUID storeId, MenuRequestDto menuRequestDto) {
        Menu menu = Menu.builder()
            .dto(menuRequestDto)
            .store(findStore(storeId)).build();
        menuRepository.save(menu);

        return MenuResponseDto.of(menu);
    }

    @Transactional
    public void updateMenu(UUID storeId, UUID menuId, MenuRequestDto menuRequestDto) {
        Menu menu = findMenu(storeId, menuId);
        menu.update(menuRequestDto);
    }

    @Transactional
    public void deleteMenu(UUID storeId, UUID menuId) {
        Menu menu = findMenu(storeId, menuId);
        menu.delete();
    }

    private Menu findMenu(UUID storeId, UUID menuId) {
        return menuRepository.findByStoreIdAndId(storeId, menuId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴입니다."));
    }

    private Store findStore(UUID storeId) {
        return storeRepository.findById(storeId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 가게입니다."));
    }
}
