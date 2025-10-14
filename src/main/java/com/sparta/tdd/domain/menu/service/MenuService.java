package com.sparta.tdd.domain.menu.service;

import com.sparta.tdd.domain.menu.dto.MenuRequestDto;
import com.sparta.tdd.domain.menu.dto.MenuResponseDto;
import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.menu.repository.MenuRepository;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.repository.StoreRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.domain.user.repository.UserRepository;
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
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
    private final UserRepository userRepository;

    public List<MenuResponseDto> getMenus(UUID storeId, UserAuthority authority) {
        List<Menu> menus;
        if (authority.isCustomerOrManager()) {
            menus = menuRepository.findAllByStoreIdAndIsHiddenFalseAndIsDeletedFalse(
                storeId);
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
            throw new BusinessException(ErrorCode.MENU_HIDDEN);
        }
        return MenuResponseDto.from(menu);
    }

    @Transactional
    public MenuResponseDto createMenu(UUID storeId, MenuRequestDto menuRequestDto, Long userId) {
        User user = findUser(userId);
        Store store = findStore(storeId);
        validateUserOnMenu(user, store);

        Menu menu = menuRequestDto.toEntity(store);
        menuRepository.save(menu);

        return MenuResponseDto.from(menu);
    }

    @Transactional
    public void updateMenu(UUID storeId, UUID menuId, MenuRequestDto menuRequestDto, Long userId) {
        User user = findUser(userId);
        Store store = findStore(storeId);
        validateUserOnMenu(user, store);

        Menu menu = findMenu(storeId, menuId);
        menu.update(menuRequestDto);
    }

    @Transactional
    public void updateMenuStatus(UUID storeId, UUID menuId, Boolean status, Long userId) {
        User user = findUser(userId);
        Store store = findStore(storeId);
        validateUserOnMenu(user, store);

        Menu menu = findMenu(storeId, menuId);
        menu.updateStatus(status);
    }

    @Transactional
    public void deleteMenu(UUID storeId, UUID menuId, Long userId) {
        User user = findUser(userId);
        Store store = findStore(storeId);
        validateUserOnMenu(user, store);

        Menu menu = findMenu(storeId, menuId);
        menu.delete(userId);
    }

    private Menu findMenu(UUID storeId, UUID menuId) {
        return menuRepository.findByStoreIdAndIdAndIsDeletedFalse(storeId, menuId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
    }

    private Store findStore(UUID storeId) {
        return storeRepository.findById(storeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateUserOnMenu(User user, Store store) {
        if (!store.isOwner(user)) {
            throw new BusinessException(ErrorCode.MENU_PERMISSION_DENIED);
        }
    }
}
