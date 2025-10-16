package com.sparta.tdd.domain.user.service;

import com.sparta.tdd.domain.order.dto.OrderResponseDto;
import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.mapper.OrderMapper;
import com.sparta.tdd.domain.order.repository.OrderRepository;
import com.sparta.tdd.domain.review.dto.response.ReviewResponseDto;
import com.sparta.tdd.domain.review.entity.Review;
import com.sparta.tdd.domain.review.repository.ReviewRepository;
import com.sparta.tdd.domain.user.dto.UserNicknameRequestDto;
import com.sparta.tdd.domain.user.dto.UserPasswordRequestDto;
import com.sparta.tdd.domain.user.dto.UserResponseDto;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.domain.user.repository.UserRepository;
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원 목록 조회
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        Page<User> userList = userRepository.findAll(pageable);

        return userList.map(UserResponseDto::from);
    }

    // 회원 정보 조회
    public UserResponseDto getUserByUserId(Long userId) {
        User user = getUserById(userId);

        return UserResponseDto.from(user);
    }

    // 회원 닉네임 수정
    @Transactional
    public UserResponseDto updateUserNickname(Long userId, Long updateId, UserNicknameRequestDto requestDto) {
        isValidUser(userId, updateId);
        User user = getUserById(userId);

        if (user.getNickname().equals(requestDto.nickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
        user.updateNickname(requestDto.nickname());

        return UserResponseDto.from(user);
    }

    // 회원 비밀번호 수정
    @Transactional
    public UserResponseDto updateUserPassword(Long userId, Long updateId, UserPasswordRequestDto requestDto) {
        isValidUser(userId, updateId);
        User user = getUserById(userId);

        if (passwordEncoder.matches(requestDto.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.SAME_PASSWORD);
        }

        user.updatePassword(passwordEncoder.encode(requestDto.password()));

        return UserResponseDto.from(user);
    }

    // 매니저 권한 부여
    @Transactional
    public UserResponseDto grantUserManagerAuthority(Long userId) {
        User user = getUserById(userId);

        if (user.getAuthority() == UserAuthority.MANAGER) {
            throw new BusinessException(ErrorCode.ALREADY_MANAGER);
        }

        user.updateAuthority(UserAuthority.MANAGER);

        return UserResponseDto.from(user);
    }

    // 리뷰 목록 조회
    public Page<ReviewResponseDto> getPersonalReviews(Long userId, Pageable pageable) {
        List<Review> reviewList = reviewRepository.findByUserIdAndNotDeleted(userId);
        Page<Review> reviews = new PageImpl<>(reviewList, pageable, reviewList.size());
        return reviews.map(ReviewResponseDto::from);
    }

    public Page<OrderResponseDto> getPersonalOrders(Long userId, Pageable pageable) {
        Page<Order> orderList = orderRepository.findOrdersByUserIdAndNotDeleted(userId, pageable);
        return orderList.map(orderMapper::toResponse);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
            () -> new BusinessException(ErrorCode.USER_NOT_FOUND)
        );
    }

    private void isValidUser(Long userId, Long validId) {
        if (userId != validId) {
            throw new BusinessException(ErrorCode.CANNOT_MODIFY_OTHER_MEMBER);
        }
    }
}
