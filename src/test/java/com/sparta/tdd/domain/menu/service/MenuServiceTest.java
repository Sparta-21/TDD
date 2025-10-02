package com.sparta.tdd.domain.menu.service;

import static jdk.jfr.internal.jfc.model.Constraint.any;

import com.sparta.tdd.domain.menu.dto.MenuRequestDto;
import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.menu.repository.MenuRepository;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import java.lang.reflect.Field;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class MenuServiceTest {

    @InjectMocks
    MenuService menuService;

    @Mock
    MenuRepository menuRepository;

    User customer;
    User owner;
    Store store;
    MenuRequestDto dto1;
    MenuRequestDto dto2;
    Menu menu1;

    @BeforeEach
    void setUp() throws Exception {
        UUID storeId = UUID.randomUUID();
        UUID menu1Id = UUID.randomUUID();

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

        menu1 = Menu.builder()
            .dto(dto1)
            .store(store).build();
        setMenuId(menu1, menu1Id);
    }

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

    @Test
    void createMenuSuccessTest() {
        // when
        when(menuRepository.save(any(Menu.class))).thenReturn();
        Menu testMenu = menuService.createMenu(store.getId(), dto);

        // then
        assertEquals(dto.name(), createdMenu.getName());
        assertEquals(dto.price(), createdMenu.getPrice());
        assertEquals(dto.imageUrl(), createdMenu.getImageUrl());
        assertEquals(dto.description(), createdMenu.getDescription());
    }

    @Test
    void getMenusTest() {
        // given

        // when

        // then
        // verify(menuRepository, times(1)).find(any(Menu.class));

    }
}
