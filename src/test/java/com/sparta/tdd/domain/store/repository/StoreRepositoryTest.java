package com.sparta.tdd.domain.store.repository;

import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.global.config.AuditConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(AuditConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StoreRepositoryTest {

    @Autowired
    private StoreRepository storeRepository;

    Store chickenStore;
    Store pizzaStore;

    @BeforeEach
    void setUp() {
        chickenStore = Store.builder()
                .name("BBQ")
                .category(StoreCategory.CHICKEN)
                .description("BBQ 광화문점")
                .imageUrl("www.test.com")
                .build();

        pizzaStore = Store.builder()
                .name("도미노피자")
                .category(StoreCategory.PIZZA)
                .description("도미노피자 광화문점")
                .imageUrl("www.test.com")
                .avgRating(BigDecimal.valueOf(4.0))        // 평균 별점 예시값
                .reviewCount(3)
                .build();

    }

    @Test
    @DisplayName("Store 저장 및 조회 테스트")
    void testSaveAndFind() {
        //given
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