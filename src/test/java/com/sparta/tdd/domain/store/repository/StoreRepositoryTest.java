package com.sparta.tdd.domain.store.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sparta.tdd.common.template.RepositoryTest;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class StoreRepositoryTest extends RepositoryTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;

    User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .username("testuser")
            .password("password123")
            .nickname("테스트유저")
            .authority(UserAuthority.CUSTOMER)
            .build();
        em.persist(testUser);
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("Store 저장 및 조회 테스트")
    void testSaveAndFind() {
        //given
        User user = em.find(User.class, testUser.getId());
        Store chickenStore = Store.builder()
            .name("BBQ")
            .category(StoreCategory.CHICKEN)
            .description("BBQ 광화문점")
            .imageUrl("www.test.com")
            .user(user)
            .build();

        Store savedStore = storeRepository.save(chickenStore);
        storeRepository.flush();

        //when
        Optional<Store> optionalStore = storeRepository.findById(savedStore.getId());

        //then
        assertTrue(optionalStore.isPresent());
        Store findStore = optionalStore.get();

        assertEquals("BBQ", findStore.getName());
        assertEquals(StoreCategory.CHICKEN, findStore.getCategory());
        assertEquals("BBQ 광화문점", findStore.getDescription());
        assertEquals("www.test.com", findStore.getImageUrl());
        assertEquals(BigDecimal.valueOf(0), findStore.getAvgRating());
        assertEquals(0, findStore.getReviewCount());
    }

    @Test
    @DisplayName("Store 저장 및 조회 테스트")
    void testFindAll() {
        //given
        User user = em.find(User.class, testUser.getId());

        Store chickenStore = Store.builder()
            .name("BBQ")
            .category(StoreCategory.CHICKEN)
            .description("BBQ 광화문점")
            .imageUrl("www.test.com")
            .user(user)
            .build();

        Store pizzaStore = Store.builder()
            .name("도미노피자")
            .category(StoreCategory.PIZZA)
            .description("도미노피자 광화문점")
            .imageUrl("www.test.com")
            .user(user)
            .build();

        storeRepository.save(chickenStore);
        storeRepository.save(pizzaStore);
        storeRepository.flush();

        //when
        List<Store> stores = storeRepository.findAll();

        // then
        assertEquals(2, stores.size(), "저장된 Store 개수와 조회된 개수가 일치해야 한다.");
        assertTrue(stores.stream()
            .anyMatch(s -> s.getName().equals("BBQ")));
        assertTrue(stores.stream()
            .anyMatch(s -> s.getName().equals("도미노피자")));
    }

    @Test
    @DisplayName("Store 이름 수정 테스트")
    void testUpdateStoreName() {
        //given
        User user = em.find(User.class, testUser.getId());
        Store chickenStore = Store.builder()
            .name("BBQ")
            .category(StoreCategory.CHICKEN)
            .description("BBQ 광화문점")
            .imageUrl("www.test.com")
            .user(user)
            .build();

        Store savedStore = storeRepository.save(chickenStore);
        storeRepository.flush();

        //when
        Store toUpdate = storeRepository.findById(savedStore.getId()).orElseThrow();
        toUpdate.updateName("BBQ 본점");
        storeRepository.flush();

        Store updatedStore = storeRepository.findById(savedStore.getId()).orElseThrow();

        //then
        assertEquals("BBQ 본점", updatedStore.getName());
    }
}