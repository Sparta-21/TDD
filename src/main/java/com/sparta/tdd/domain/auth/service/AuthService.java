package com.sparta.tdd.domain.auth.service;

import com.sparta.tdd.domain.auth.dto.AuthInfo;
import com.sparta.tdd.domain.auth.dto.request.LoginRequest;
import com.sparta.tdd.domain.auth.dto.request.SignUpRequest;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.domain.user.repository.UserRepository;
import com.sparta.tdd.global.jwt.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider accessTokenProvider;
    private final JwtTokenProvider refreshTokenProvider;


    @Transactional
    public AuthInfo signUp(SignUpRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("이미 존재하는 username 입니다.");
        } else {
            User newUser = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .authority(UserAuthority.CUSTOMER)
                .build();
            User savedUser = userRepository.save(newUser);

            String accessToken = accessTokenProvider.generateToken(
                savedUser.getUsername(), savedUser.getId(), savedUser.getAuthority());
            String refreshToken = refreshTokenProvider.generateToken(
                savedUser.getUsername(), savedUser.getId(), savedUser.getAuthority());

            return new AuthInfo(savedUser.getId(), accessToken, refreshToken);
        }
    }

    public AuthInfo login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new IllegalArgumentException("올바르지 않은 요청입니다."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("올바르지 않은 요청입니다.");
        }

        String accessToken = accessTokenProvider.generateToken(
            user.getUsername(), user.getId(), user.getAuthority());
        String refreshToken = refreshTokenProvider.generateToken(
            user.getUsername(), user.getId(), user.getAuthority());

        return new AuthInfo(user.getId(), accessToken, refreshToken);
    }
}
