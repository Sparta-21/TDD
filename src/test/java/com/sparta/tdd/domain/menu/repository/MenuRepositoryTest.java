package com.sparta.tdd.domain.menu.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.global.config.AuditConfig;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuditConfig.class)
class MenuRepositoryTest {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private EntityManager em;

    private User testUser;
    private Store testStore;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .password("password123")
                .nickname("테스트유저")
                .authority(UserAuthority.CUSTOMER)
                .build();
        em.persist(testUser);

        testStore = Store.builder()
                .name("테스트 가게")
                .category(StoreCategory.KOREAN)
                .description("맛있는 가게")
                .user(testUser)
                .build();
        em.persist(testStore);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("생성 및 조회 테스트")
    void createAndReadTest() {
        // given
        Store store = em.find(Store.class, testStore.getId());
        Menu menu = Menu.builder()
                .name("test")
                .description("this is testing")
                .price(10000)
                .imageUrl("1234")
                .isHidden(false)
                .store(store)
                .build();
        menuRepository.save(menu);
        menuRepository.flush();

        // when
        Optional<Menu> foundMenu = menuRepository.findById(menu.getId());

        // then
        assertTrue(foundMenu.isPresent());
        Menu testMenu = foundMenu.get();
        assertEquals("test", testMenu.getName());
        assertEquals("this is testing", testMenu.getDescription());
        assertEquals(10000, testMenu.getPrice());
        assertEquals("1234", testMenu.getImageUrl());
        assertFalse(testMenu.getIsHidden());
    }

    @Test
    @DisplayName("수정테스트")
    void updateTest() {
        // given
        Store store = em.find(Store.class, testStore.getId());
        Menu menu = Menu.builder()
                .name("test")
                .description("this is testing")
                .price(10000)
                .imageUrl("1234")
                .isHidden(false)
                .store(store)
                .build();
        menuRepository.save(menu);
        menuRepository.flush();

        // when
        Menu testMenu = menuRepository.findById(menu.getId()).orElse(null);
        testMenu.updateName("updateTest");
        menuRepository.flush();

        // then
        Optional<Menu> foundMenu = menuRepository.findById(menu.getId());
        assertEquals("updateTest", foundMenu.get().getName());
    }

    @Test
    @DisplayName("삭제테스트")
    void deleteTest() {
        // given
        Store store = em.find(Store.class, testStore.getId());
        Menu menu = Menu.builder()
                .name("test")
                .description("this is testing")
                .price(10000)
                .imageUrl("1234")
                .isHidden(false)
                .store(store)
                .build();
        menuRepository.save(menu);
        menuRepository.flush();

        // when
        Menu testMenu = menuRepository.findById(menu.getId()).orElse(null);
        menuRepository.delete(testMenu);
        menuRepository.flush();

        // then
        Optional<Menu> foundMenu = menuRepository.findById(menu.getId());
        assertFalse(foundMenu.isPresent());
    }

}
