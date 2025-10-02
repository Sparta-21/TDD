package com.sparta.tdd.domain.menu.service;

import com.sparta.tdd.domain.menu.dto.MenuRequestDto;
import com.sparta.tdd.domain.menu.dto.MenuResponseDto;
import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.menu.repository.MenuRepository;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.repository.StoreRepository;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;

    public List<MenuResponseDto> getMenus(UUID storeId, UserAuthority authority) {
        List<Menu> menus;
        if (authority.isCustomerOrManager()) {
            menus = menuRepository.findAllByStoreIdAndIsHiddenFalse(storeId);
        } else {
            menus = menuRepository.findAllByStoreId(storeId);
        }
        return menus.stream()
            .map(MenuResponseDto::from)
            .toList();
    }

    public MenuResponseDto getMenu(UUID storeId, UUID menuId, UserAuthority authority) {
        Menu menu = findMenu(storeId, menuId);
        if (authority.isCustomerOrManager() && menu.isHidden()) {
            throw new IllegalArgumentException("숨겨진 메뉴입니다.");
        }
        return MenuResponseDto.from(menu);
    }

    @Transactional
    public MenuResponseDto createMenu(UUID storeId, MenuRequestDto menuRequestDto) {
        Menu menu = Menu.builder()
            .dto(menuRequestDto)
            .store(findStore(storeId)).build();
        menuRepository.save(menu);

        return MenuResponseDto.from(menu);
    }

    @Transactional
    public void updateMenu(UUID storeId, UUID menuId, MenuRequestDto menuRequestDto) {
        Menu menu = findMenu(storeId, menuId);
        menu.update(menuRequestDto);
    }

    @Transactional
    public void updateMenuStatus(UUID storeId, UUID menuId, Boolean status) {
        Menu menu = findMenu(storeId, menuId);
        menu.updateStatus(status);
    }

    @Transactional
    public void deleteMenu(UUID storeId, UUID menuId, Long userId) {
        Menu menu = findMenu(storeId, menuId);
        menu.delete(userId);
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
