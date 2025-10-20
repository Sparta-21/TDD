package com.sparta.tdd.domain.menu.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.tdd.domain.menu.dto.MenuRequestDto;
import com.sparta.tdd.domain.menu.dto.MenuResponseDto;
import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.menu.repository.MenuRepository;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.domain.store.repository.StoreRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.domain.user.repository.UserRepository;
import com.sparta.tdd.global.exception.BusinessException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MenuServiceTest {

    @InjectMocks
    MenuService menuService;

    @Mock
    MenuRepository menuRepository;

    @Mock
    StoreRepository storeRepository;

    @Mock
    UserRepository userRepository;

    User customer;
    User owner;
    Store store;
    MenuRequestDto dto1;
    MenuRequestDto dto2;
    MenuRequestDto dto3;
    Menu menu1;
    Menu menu2;

    @BeforeEach
    void setUp() throws Exception {
        UUID storeId = UUID.randomUUID();
        UUID menu1Id = UUID.randomUUID();
        UUID menu2Id = UUID.randomUUID();

        customer = User.builder()
            .username("customer")
            .password("password1")
            .nickname("test1")
            .authority(UserAuthority.CUSTOMER).build();
        setUserId(customer, 1L);

        owner = User.builder()
            .username("owner")
            .password("password2")
            .nickname("test2")
            .authority(UserAuthority.OWNER).build();
        setUserId(owner, 2L);

        store = Store.builder()
            .name("store")
            .category(StoreCategory.KOREAN)
            .description("this is store description")
            .imageUrl("this is image url")
            .user(owner).build();
        setStoreId(store, storeId);

        dto1 = MenuRequestDto.builder()
            .name("menu1")
            .description("this is menu1")
            .price(5000)
            .imageUrl("this is image url").build();

        dto2 = MenuRequestDto.builder()
            .name("menu2")
            .description("this is menu2")
            .price(10000)
            .imageUrl("this is image url").build();

        dto3 = MenuRequestDto.builder()
            .name("menu3")
            .description("this is menu3")
            .price(15000)
            .imageUrl("this is image url").build();

        // 정상 메뉴
        menu1 = dto1.toEntity(store);
        setMenuId(menu1, menu1Id);

        // 숨겨진 메뉴
        menu2 = dto2.toEntity(store);
        setMenuId(menu2, menu2Id);
        setMenuIsHidden(menu2, true);
    }

    @Nested
    @DisplayName("권한별 메뉴 목록 조회")
    class GetMenus {

        @Test
        @DisplayName("Customer 메뉴 목록 조회 테스트")
        void getMenusCustomerTest() {
            // given
            when(menuRepository.findAllByStoreIdAndIsHiddenFalseAndIsDeletedFalse(store.getId()))
                .thenReturn(List.of(menu1));

            // when
            List<MenuResponseDto> testMenus = menuService.getMenus(store.getId(),
                customer.getAuthority());

            // then
            assertNotNull(testMenus);
            verify(menuRepository, times(1)).findAllByStoreIdAndIsHiddenFalseAndIsDeletedFalse(
                store.getId());
            verify(menuRepository, never()).findAllByStoreId(any());
            assertTrue(testMenus.stream().anyMatch(m -> m.menuId().equals(menu1.getId())));
            assertTrue(testMenus.stream().noneMatch(m -> m.menuId().equals(menu2.getId())));
        }

        @Test
        @DisplayName("OWNER 메뉴 목록 조회 테스트")
        void getMenusOwnerTest() {
            // given
            when(menuRepository.findAllByStoreId(store.getId()))
                .thenReturn(List.of(menu1, menu2));

            // when
            List<MenuResponseDto> testMenus = menuService.getMenus(store.getId(),
                owner.getAuthority());

            // then
            assertNotNull(testMenus);
            verify(menuRepository, times(1)).findAllByStoreId(store.getId());
            verify(menuRepository, never()).findAllByStoreIdAndIsHiddenFalseAndIsDeletedFalse(
                any());
            assertTrue(testMenus.stream().anyMatch(m -> m.menuId().equals(menu1.getId())));
            assertTrue(testMenus.stream().anyMatch(m -> m.menuId().equals(menu2.getId())));
        }
    }

    @Nested
    @DisplayName("권한별 메뉴 상세 조회")
    class GetMenu {

        @Test
        @DisplayName("Customer 메뉴 상세 테스트")
        void getMenusCustomerTest() {
            //given
            when(menuRepository.findByIdAndStoreIdAndIsDeletedFalse(menu2.getId(), store.getId()))
                .thenReturn(Optional.of(menu2));

            // when & then
            assertThrows(BusinessException.class,
                () -> menuService.getMenu(store.getId(), menu2.getId(),
                    customer.getAuthority()));
            verify(menuRepository, times(1)).findByIdAndStoreIdAndIsDeletedFalse(menu2.getId(), store.getId());
        }

        @Test
        @DisplayName("OWNER 메뉴 상세 테스트")
        void getMenusOwnerTest() {
            //given
            when(menuRepository.findByIdAndStoreIdAndIsDeletedFalse(menu2.getId(), store.getId()))
                .thenReturn(Optional.of(menu2));

            // when
            MenuResponseDto testMenu = menuService.getMenu(store.getId(), menu2.getId(),
                owner.getAuthority());

            // then
            assertNotNull(testMenu);
            assertEquals(menu2.getId(), testMenu.menuId());
            verify(menuRepository, times(1)).findByIdAndStoreIdAndIsDeletedFalse(menu2.getId(), store.getId());
        }

    }

    @Test
    @DisplayName("메뉴 등록 테스트")
    void createMenuSuccessTest() {
        // given
        when(storeRepository.findById(store.getId()))
            .thenReturn(Optional.of(store));
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));

        // when
        MenuResponseDto testMenu = menuService.createMenu(store.getId(), dto3, 2L);

        // then
        assertNotNull(testMenu);
        assertEquals(dto3.name(), testMenu.name());
        assertEquals(dto3.description(), testMenu.description());
        assertEquals(dto3.price(), testMenu.price());
        assertEquals(dto3.imageUrl(), testMenu.imageUrl());
        assertEquals(Boolean.FALSE, testMenu.isHidden());
        verify(menuRepository, times(1)).save(any(Menu.class));
        verify(storeRepository, times(1)).findById(store.getId());
    }

    @Test
    @DisplayName("메뉴 수정 테스트")
    void updateMenuSuccessTest() {
        // given
        when(menuRepository.findByIdAndStoreIdAndIsDeletedFalse(menu1.getId(), store.getId()))
            .thenReturn(Optional.of(menu1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));

        // when
        menuService.updateMenu(store.getId(), menu1.getId(), dto3, 2L);

        // then
        verify(menuRepository, times(1)).findByIdAndStoreIdAndIsDeletedFalse(menu1.getId(), store.getId());
        assertEquals(dto3.name(), menu1.getName());
        assertEquals(dto3.description(), menu1.getDescription());
        assertEquals(dto3.price(), menu1.getPrice());
        assertEquals(dto3.imageUrl(), menu1.getImageUrl());
        assertEquals(Boolean.FALSE, menu1.isHidden());
    }

    @Test
    @DisplayName("메뉴 상태 수정 테스트")
    void updateMenuStatusSuccessTest() {
        // given
        when(menuRepository.findByIdAndStoreIdAndIsDeletedFalse(menu1.getId(), store.getId()))
            .thenReturn(Optional.of(menu1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));

        // when
        menuService.updateMenuStatus(store.getId(), menu1.getId(), Boolean.TRUE, 2L);

        // then
        verify(menuRepository, times(1)).findByIdAndStoreIdAndIsDeletedFalse(menu1.getId(), store.getId());
        assertTrue(menu1.isHidden());
    }

    @Test
    @DisplayName("메뉴 삭제 테스트(soft delete)")
    void deleteMenuSuccessTest() {
        // given
        when(menuRepository.findByIdAndStoreIdAndIsDeletedFalse(menu1.getId(), store.getId()))
            .thenReturn(Optional.of(menu1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));

        // when
        menuService.deleteMenu(store.getId(), menu1.getId(), owner.getId());

        // then
        verify(menuRepository, times(1)).findByIdAndStoreIdAndIsDeletedFalse(menu1.getId(), store.getId());
        assertNotNull(menu1.getDeletedAt());
        assertEquals(owner.getId(), menu1.getDeletedBy());
    }

    // Reflection
    private void setUserId(User user, Long id) throws Exception {
        Field field = User.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(user, id);
    }

    private void setStoreId(Store store, UUID id) throws Exception {
        Field field = Store.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(store, id);
    }

    private void setMenuId(Menu menu, UUID id) throws Exception {
        Field field = Menu.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(menu, id);
    }

    private void setMenuIsHidden(Menu menu, Boolean isHidden) throws Exception {
        Field field = Menu.class.getDeclaredField("isHidden");
        field.setAccessible(true);
        field.set(menu, isHidden);
    }
}
