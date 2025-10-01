package com.sparta.tdd.domain.user.service;

import com.sparta.tdd.domain.user.dto.*;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // 회원 목록 조회
    public Page<UserResponseDto> getAllUsers(int page, int size, String sortBy, boolean isAsc, UserAuthority authority) {
        if (authority != UserAuthority.MASTER && authority != UserAuthority.MANAGER) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

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
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
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
            throw new IllegalArgumentException("이미 사용중인 비밀번호입니다.");
        }

        user.updatePassword(passwordEncoder.encode(requestDto.password()));

        return UserResponseDto.from(user);
    }
    // 매니저 권한 부여
    @Transactional
    public UserResponseDto grantUserManagerAuthority(Long userId, UserAuthority authority) {
        if (authority != UserAuthority.MASTER) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        User user = getUserById(userId);

        if (user.getAuthority() == UserAuthority.MANAGER) {
            throw new IllegalArgumentException("이미 매니저 권한입니다.");
        }

        user.updateAuthority(UserAuthority.MANAGER);

        return UserResponseDto.from(user);
    }
    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 회원입니다.")
        );
    }
    private void isValidUser(Long userId, Long validId) {
        if (userId != validId) {
            throw new IllegalArgumentException("다른 사용자의 정보를 수정할 수 없습니다.");
        }
    }
}
