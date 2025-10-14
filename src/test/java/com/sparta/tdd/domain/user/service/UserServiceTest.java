package com.sparta.tdd.domain.user.service;

import com.sparta.tdd.domain.order.dto.OrderResponseDto;
import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.mapper.OrderMapper;
import com.sparta.tdd.domain.order.repository.OrderRepository;
import com.sparta.tdd.domain.review.entity.Review;
import com.sparta.tdd.domain.review.repository.ReviewRepository;
import com.sparta.tdd.domain.user.dto.UserNicknameRequestDto;
import com.sparta.tdd.domain.user.dto.UserResponseDto;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.domain.user.repository.UserRepository;
import com.sparta.tdd.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    ReviewRepository reviewRepository;
    @Mock
    OrderMapper orderMapper;
    @Mock
    OrderRepository orderRepository;
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
        assertThrows(BusinessException.class, () -> {
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
        assertThrows(BusinessException.class, () -> {
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
        assertThrows(BusinessException.class, () -> {
            //when
            userService.updateUserNickname(1L, 1L, requestDto);
        });
    }

    @Test
    @DisplayName("유저가 작성한 리뷰 조회 성공")
    void findReviewByUserSuccess() {
        // given
        User user1 = mock(User.class);
        Pageable pageable = mock(Pageable.class);
        Review review1 = new Review(user1, null, null, null, null, "맛없어요ㅠ");
        Review review2 = new Review(user1, null, null, null, null, "맛있어요!");

        List<Review> reviewList = List.of(review1, review2);
        when(user1.getId()).thenReturn(1L);
        when(reviewRepository.findByUserIdAndNotDeleted(user1.getId())).thenReturn(reviewList);

        // when
        List<Review> byUserIdAndNotDeleted = reviewRepository.findByUserIdAndNotDeleted(user1.getId());
        Page<Review> reviews = new PageImpl<>(byUserIdAndNotDeleted, pageable, byUserIdAndNotDeleted.size());

        // then
        assertEquals(reviews.getTotalElements(), 2);
        assertTrue(reviews.stream().anyMatch(review -> {
            return review.getContent().equals("맛있어요!");
        }));
    }

    @Test
    @DisplayName("유저가 작성한 리뷰 조회 실패")
    void findReviewByUserFail() {
        // given
        User user1 = mock(User.class);
        Pageable pageable = mock(Pageable.class);
        Review review1 = new Review(user1, null, null, null, null, "맛없어요ㅠ");
        Review review2 = new Review(user1, null, null, null, null, "맛있어요!");

        List<Review> reviewList = List.of(review1, review2);
        when(user1.getId()).thenReturn(1L);
        when(reviewRepository.findByUserIdAndNotDeleted(user1.getId())).thenReturn(reviewList);

        // when
        List<Review> byUserIdAndNotDeleted = reviewRepository.findByUserIdAndNotDeleted(user1.getId());
        Page<Review> reviews = new PageImpl<>(byUserIdAndNotDeleted, pageable, byUserIdAndNotDeleted.size());

        // then
        assertEquals(reviews.getTotalElements(), 2);
        assertFalse(reviews.stream().anyMatch(review -> {
            return review.getContent().equals("맛있는지 모르겠어요.");
        }));
    }

    @Test
    @DisplayName("유저 주문 목록 조회 성공")
    public void findOrdersByUserSuccess() {
        // given
        User user = mock(User.class);
        Pageable pageable = PageRequest.of(0, 10);

        Order order1 = new Order();
        Order order2 = new Order();
        List<Order> orderList = List.of(order1, order2);
        Page<Order> orders = new PageImpl<>(orderList, pageable, orderList.size());


        when(user.getId()).thenReturn(1L);
        when(orderRepository.findOrdersByUserIdAndNotDeleted(user.getId(), pageable))
                .thenReturn(orders);
        when(orderMapper.toResponse(order1)).thenReturn(mock(OrderResponseDto.class));
        when(orderMapper.toResponse(order2)).thenReturn(mock(OrderResponseDto.class));
        // when
        Page<OrderResponseDto> personalOrders = userService.getPersonalOrders(user.getId(), pageable);

        // then
        assertEquals(2, personalOrders.getContent().size());
        verify(orderRepository, times(1)).findOrdersByUserIdAndNotDeleted(user.getId(), pageable);
        verify(orderMapper, times(1)).toResponse(order1);
        verify(orderMapper, times(1)).toResponse(order2);

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