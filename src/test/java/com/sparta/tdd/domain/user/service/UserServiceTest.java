package com.sparta.tdd.domain.user.service;

import com.sparta.tdd.domain.user.dto.UserNicknameRequestDto;
import com.sparta.tdd.domain.user.dto.UserResponseDto;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @Test
    @DisplayName("모든 유저 조회")
    void getAllUserTest() {
        //given
        User user1 = createUser("test01", "1234", "test1", UserAuthority.CUSTOMER);
        User user2 = createUser("test02", "2345", "test2", UserAuthority.MANAGER);
        User user3 = createUser("test03", "3456", "test3", UserAuthority.MASTER);

        List<User> users = List.of(user1, user2, user3);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("username"));
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // when
        Page<UserResponseDto> result = userService.getAllUsers(pageable);

        // then
        assertEquals(3, result.getTotalElements());
        assertEquals("test01", result.getContent().get(0).username());
        assertEquals("test03", result.getContent().get(2).username());

        verify(userRepository, times(1)).findAll(pageable);
    }
    @Test
    @DisplayName("회원 정보 단건 조회")
    void readUserInfoByUserIdTest() {
        User user1 = createUser("test01", "1234", "test1", UserAuthority.CUSTOMER);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));

        UserResponseDto result = userService.getUserByUserId(1L);

        assertEquals(result.id(), user1.getId());
        assertEquals(result.authority(), user1.getAuthority().getDescription());
        assertEquals(result.nickname(), user1.getNickname());
    }
    @Test
    @DisplayName("권한이 CUSTOMER인 유저를 MANAGER로 변경")
    void grantManagerAuthorityTest() {
        //given
        User user1 = createUser("test01", "1234", "test1", UserAuthority.CUSTOMER);
        User user2 = createUser("test02", "1234", "test2", UserAuthority.MASTER);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        //when
        UserResponseDto result = userService.grantUserManagerAuthority(1L);
        //then
        assertEquals(UserAuthority.MANAGER, user1.getAuthority());
        assertEquals(user1.getId(), result.id());
        assertEquals(user1.getUsername(), result.username());
    }
    @Test
    @DisplayName("이미 MANAGER 권한이면 예외 발생")
    void grantManagerAuthorityAlreadyManager() {
        // given
        User user = createUser("test01", "1234", "test1", UserAuthority.CUSTOMER);
        User user2 = createUser("test02", "1234", "test2", UserAuthority.MASTER);

        user.updateAuthority(UserAuthority.MANAGER);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        // then
        assertThrows(IllegalArgumentException.class, () -> {
            // when
            userService.grantUserManagerAuthority(1L);
        });
    }
    @Test
    @DisplayName("MASTER 권한이 아닌 유저가 권한 부여를 하면 예외 발생")
    void grantManagerAuthorityCustomer() {
        // given
        User user = createUser("test01", "1234", "test1", UserAuthority.CUSTOMER);

        // then
        assertThrows(IllegalArgumentException.class, () -> {
            // when
            userService.grantUserManagerAuthority(1L);
        });
    }
    @Test
    @DisplayName("회원 닉네임 변경 성공")
    void updateUserNicknameSuccessTest() {
        // given
        User user1 = createUser("test01", "1234", "test1", UserAuthority.CUSTOMER);
        UserNicknameRequestDto requestDto = new UserNicknameRequestDto("newNickname");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        //when
        UserResponseDto result = userService.updateUserNickname(1L, 1L, requestDto);
        //then
        assertEquals(requestDto.nickname(), result.nickname());
    }
    @Test
    @DisplayName("회원 닉네임 변경 실패")
    void updateUserNicknameFailTest() {
        // given
        User user1 = createUser("test01", "1234", "test1", UserAuthority.CUSTOMER);
        UserNicknameRequestDto requestDto = new UserNicknameRequestDto("test1");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));

        //then
        assertThrows(IllegalArgumentException.class, () -> {
            //when
            userService.updateUserNickname(1L, 1L, requestDto);
        });
    }

    User createUser(String username, String password, String nickname, UserAuthority authority) {
        return User.builder()
                .username(username)
                .password(password)
                .nickname(nickname)
                .authority(authority)
                .build();
    }
}