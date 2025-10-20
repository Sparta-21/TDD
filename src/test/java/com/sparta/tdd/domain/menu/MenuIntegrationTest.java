package com.sparta.tdd.domain.menu;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sparta.tdd.common.helper.CustomWithMockUser;
import com.sparta.tdd.common.template.IntegrationTest;
import com.sparta.tdd.domain.menu.dto.MenuRequestDto;
import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.menu.repository.MenuRepository;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.domain.store.repository.StoreRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.domain.user.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

public class MenuIntegrationTest extends IntegrationTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    MenuRepository menuRepository;

    User owner;
    User customer;
    Store store;
    Menu menu;

    @BeforeEach
    void setUp() {
        customer = User.builder()
            .username("customer")
            .password("password1")
            .nickname("test1")
            .authority(UserAuthority.CUSTOMER)
            .build();
        userRepository.save(customer);

        owner = User.builder()
            .username("owner")
            .password("password2")
            .nickname("test2")
            .authority(UserAuthority.OWNER)
            .build();
        userRepository.save(owner);

        store = Store.builder()
            .name("store")
            .category(StoreCategory.KOREAN)
            .description("this is store description")
            .imageUrl("this is image url")
            .user(owner)
            .build();
        storeRepository.save(store);

        menu = Menu.builder()
            .name("menu")
            .description("this is menu")
            .price(30000)
            .imageUrl("this is image url")
            .store(store)
            .build();
        menuRepository.save(menu);
    }

    @Nested
    @DisplayName("메뉴 등록 테스트")
    class createMenu {

        @Test
        @CustomWithMockUser(userId = 2L, username = "owner", authority = UserAuthority.OWNER)
        @DisplayName("OWNER 메뉴 등록 성공")
        void ownerCreatesMenu() throws Exception {
            // given
            UUID storeId = store.getId();

            MenuRequestDto dto = MenuRequestDto.builder()
                .name("menu1")
                .description("this is menu1")
                .price(15000)
                .imageUrl("this is image url")
                .build();

            // when & then
            mockMvc.perform(
                    post("/v1/store/{storeId}/menu", storeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpectAll(status().isCreated(),
                    jsonPath("$.name").value(dto.name()),
                    jsonPath("$.description").value(dto.description()),
                    jsonPath("$.price").value(dto.price()),
                    jsonPath("$.imageUrl").value(dto.imageUrl()));
        }

        @Test
        @CustomWithMockUser(username = "customer")
        @DisplayName("CUSTOMER 메뉴 등록 실패")
        void customerCreatesMenu() throws Exception {
            // given
            UUID storeId = store.getId();

            MenuRequestDto dto = MenuRequestDto.builder()
                .name("menu1")
                .description("this is menu1")
                .price(15000)
                .imageUrl("this is image url")
                .build();

            // when & then
            mockMvc.perform(
                    post("/v1/store/{storeId}/menu", storeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("메뉴 수정 테스트")
    class updateMenu {

        @Test
        @CustomWithMockUser(userId = 2L, username = "owner", authority = UserAuthority.OWNER)
        @DisplayName("OWNER 메뉴 수정 성공")
        void ownerUpdatesMenu() throws Exception {
            // given
            UUID storeId = store.getId();
            UUID menuId = menu.getId();

            MenuRequestDto dto = MenuRequestDto.builder()
                .name("menu update")
                .description("this is menu update")
                .price(10000)
                .imageUrl("this is image url update")
                .build();

            // when & then
            mockMvc.perform(
                    patch("/v1/store/{storeId}/menu/{menuId}", storeId, menuId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpectAll(status().isNoContent());

            // then
            mockMvc.perform(
                    get("/v1/store/{storeId}/menu/{menuId}", storeId, menuId)
                        .with(csrf()))
                .andExpectAll(status().isOk(),
                    jsonPath("$.name").value(dto.name()),
                    jsonPath("$.description").value(dto.description()),
                    jsonPath("$.price").value(dto.price()),
                    jsonPath("$.imageUrl").value(dto.imageUrl()));
        }

        @Test
        @CustomWithMockUser(username = "customer")
        @DisplayName("CUSTOMER 메뉴 수정 실패")
        void customerUpdatesMenu() throws Exception {
            // given
            UUID storeId = store.getId();
            UUID menuId = menu.getId();

            MenuRequestDto dto = MenuRequestDto.builder()
                .name("menu update")
                .description("this is menu update")
                .price(10000)
                .imageUrl("this is image url update")
                .build();

            // when & then
            mockMvc.perform(
                    patch("/v1/store/{storeId}/menu/{menuId}", storeId, menuId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isForbidden());
        }

        @Test
        @CustomWithMockUser(userId = 2L, username = "owner", authority = UserAuthority.OWNER)
        @DisplayName("OWNER 메뉴 상태 수정 성공")
        void ownerUpdatesMenuStatus() throws Exception {
            // given
            UUID storeId = store.getId();
            UUID menuId = menu.getId();

            // when & then
            mockMvc.perform(
                    patch("/v1/store/{storeId}/menu/{menuId}/status", storeId, menuId)
                        .param("status", "true")
                        .with(csrf()))
                .andExpectAll(status().isNoContent());

            // then
            mockMvc.perform(
                    get("/v1/store/{storeId}/menu/{menuId}", storeId, menuId)
                        .with(csrf()))
                .andExpectAll(status().isOk(),
                    jsonPath("$.isHidden").value(Boolean.TRUE));
        }

        @Test
        @CustomWithMockUser(username = "customer")
        @DisplayName("CUSTOMER 메뉴 상태 수정 실패")
        void customerUpdatesMenuStatus() throws Exception {
            // given
            UUID storeId = store.getId();
            UUID menuId = menu.getId();

            // when
            mockMvc.perform(
                    patch("/v1/store/{storeId}/menu/{menuId}/status", storeId, menuId)
                        .param("status", "true")
                        .with(csrf()))
                .andExpectAll(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("메뉴 삭제 테스트")
    class deleteMenu {

        @Test
        @CustomWithMockUser(userId = 2L, username = "owner", authority = UserAuthority.OWNER)
        @DisplayName("OWNER 메뉴 삭제 성공")
        void ownerDeletesMenu() throws Exception {
            // given
            UUID storeId = store.getId();
            UUID menuId = menu.getId();

            // when & then
            mockMvc.perform(
                    delete("/v1/store/{storeId}/menu/{menuId}", storeId, menuId)
                        .with(csrf()))
                .andExpectAll(status().isNoContent());

            // then
            mockMvc.perform(
                    get("/v1/store/{storeId}/menu/{menuId}", storeId, menuId)
                        .with(csrf()))
                .andExpectAll(status().isNotFound());
        }

        @Test
        @CustomWithMockUser(username = "customer")
        @DisplayName("CUSTOMER 메뉴 삭제 실패")
        void customerDeletesMenu() throws Exception {
            //given
            UUID storeId = store.getId();
            UUID menuId = menu.getId();

            // when & then
            mockMvc.perform(
                    delete("/v1/store/{storeId}/menu/{menuId}", storeId, menuId)
                        .with(csrf()))
                .andExpectAll(status().isForbidden());
        }
    }

}
