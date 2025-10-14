package com.sparta.tdd.domain.user.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.global.config.AuditConfig;
import com.sparta.tdd.global.config.QueryDSLConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("local")
@Import({AuditConfig.class, QueryDSLConfig.class})
class UserRepositoryTest {

    private User user;
    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void init() {
        user = User.builder()
            .username("test01")
            .password("test12345")
            .nickname("test")
            .authority(UserAuthority.CUSTOMER)
            .build();
    }

    @Test
    void signup() {
        //when
        userRepository.save(user);

        //then
        assertEquals(true, userRepository.existsByUsername("test01"));
    }

    @Test
    void readUserProfile() {
        //given
        userRepository.save(user);

        //when
        User findUser = userRepository.findByUsername("test01").get();

        //then
        assertEquals(user.getId(), findUser.getId());
        assertEquals(user.getUsername(), findUser.getUsername());
        assertEquals(UserAuthority.CUSTOMER, findUser.getAuthority());
    }

    @Test
    void updateUserProfile() {
        //given
        userRepository.save(user);

        //when
        User findUser = userRepository.findByUsername("test01").get();
        findUser.updateNickname("test2");
        findUser.updateAuthority(UserAuthority.MANAGER);

        User updatedUser = userRepository.findByUsername("test01").get();

        //then
        assertEquals(findUser.getNickname(), updatedUser.getNickname());
        assertEquals(findUser.getAuthority(), updatedUser.getAuthority());
    }

    @Test
    void deleteUser() {
        //given
        userRepository.save(user);

        //when
        userRepository.delete(user);

        //then
        assertEquals(false, userRepository.existsByUsername("test01"));
    }
}