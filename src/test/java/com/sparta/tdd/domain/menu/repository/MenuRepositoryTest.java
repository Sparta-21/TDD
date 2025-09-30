package com.sparta.tdd.domain.menu.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.global.config.AuditConfig;
import java.util.Optional;
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

    @Test
    @DisplayName("생성 및 조회 테스트")
    void createAndReadTest() {
        // given
        Menu menu = new Menu(null, "test", "this is testing", 10000, "1234", false);
        menuRepository.save(menu);
        menuRepository.flush();

        // when
        Optional<Menu> foundMenu = menuRepository.findById(menu.getMenuId());

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
        Menu menu = new Menu(null, "test", "this is testing", 10000, "1234", null);
        menuRepository.save(menu);
        menuRepository.flush();

        // when
        Menu testMenu = menuRepository.findById(menu.getMenuId()).orElse(null);
        testMenu.updateName("updateTest");
        menuRepository.flush();

        // then
        Optional<Menu> foundMenu = menuRepository.findById(menu.getMenuId());
        assertEquals("updateTest", foundMenu.get().getName());
    }

    @Test
    @DisplayName("삭제테스트")
    public void deleteTest() {
        // given
        Menu menu = new Menu(null, "test", "this is testing", 10000, "1234", null);
        menuRepository.save(menu);
        menuRepository.flush();

        // when
        Menu testMenu = menuRepository.findById(menu.getMenuId()).orElse(null);
        menuRepository.delete(testMenu);
        menuRepository.flush();

        // then
        Optional<Menu> foundMenu = menuRepository.findById(menu.getMenuId());
        assertFalse(foundMenu.isPresent());
    }

}
