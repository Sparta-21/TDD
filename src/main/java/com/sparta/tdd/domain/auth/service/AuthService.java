package com.sparta.tdd.domain.auth.service;

import com.sparta.tdd.domain.auth.dto.AuthInfo;
import com.sparta.tdd.domain.auth.dto.request.LoginRequestDto;
import com.sparta.tdd.domain.auth.dto.request.SignUpRequestDto;
import com.sparta.tdd.domain.review.repository.ReviewRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.domain.user.repository.UserRepository;
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
import com.sparta.tdd.global.jwt.JwtTokenValidator;
import com.sparta.tdd.global.jwt.RequestTokenExtractor;
import com.sparta.tdd.global.jwt.provider.AccessTokenProvider;
import com.sparta.tdd.global.jwt.provider.RefreshTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenProvider accessTokenProvider;
    private final RefreshTokenProvider refreshTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final WithdrawalDataCleanService withdrawalDataCleanService;
    private final JwtTokenValidator jwtTokenValidator;

    @Transactional
    public AuthInfo signUp(SignUpRequestDto request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        UserAuthority userAuthority = null;
        if (request.authority() != null) {
            userAuthority = request.authority();
        }

        User newUser = User.builder()
            .username(request.username())
            .password(passwordEncoder.encode(request.password()))
            .authority(userAuthority)
            .nickname(request.nickname())
            .build();
        User savedUser = userRepository.save(newUser);

        String accessToken = accessTokenProvider.generateToken(
            savedUser.getUsername(), savedUser.getId(), savedUser.getAuthority());
        String refreshToken = refreshTokenProvider.generateToken(
            savedUser.getUsername(), savedUser.getId(), savedUser.getAuthority());

        return new AuthInfo(savedUser.getId(), accessToken, refreshToken);
    }

    public AuthInfo login(LoginRequestDto request) {
        User user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN_CREDENTIALS);
        }

        String accessToken = accessTokenProvider.generateToken(user.getUsername(), user.getId(), user.getAuthority());
        String refreshToken = refreshTokenProvider.generateToken(user.getUsername(), user.getId(), user.getAuthority());

        return new AuthInfo(user.getId(), accessToken, refreshToken);
    }

    public void logout(HttpServletRequest request) {
        Optional<String> accessToken = RequestTokenExtractor.extractAccessToken(request);
        Optional<String> refreshToken = RequestTokenExtractor.extractRefreshToken(request);

        accessToken.ifPresent(tokenBlacklistService::addAccessTokenToBlacklist);
        refreshToken.ifPresent(tokenBlacklistService::addRefreshTokenToBlacklist);
    }

    @Transactional
    public void withdrawal(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        LocalDateTime deletedAt = LocalDateTime.now();

        // 가게 사장님이라면, 보유한 모든 음식점 soft delete
        // 음식점이 삭제되기 떄문에 연관된 메뉴, 리뷰, 리뷰댓글 등도 모두 soft delete
        if (UserAuthority.isOwner(user.getAuthority())) {
            withdrawalDataCleanService.deleteOwnerRelatedData(userId, deletedAt);
        } else {
            // 리뷰의 경우 음식점 사장님 데이터 제거 시 한 번 호출 돼 if 문 밖에서 호출할 경우 중복호출 발생 가능
            reviewRepository.bulkSoftDeleteByUserId(userId, deletedAt, userId);
        }
        // 유저 개인적인 데이터 삭제
        withdrawalDataCleanService.deleteCommonUserData(userId, deletedAt);

        user.delete(userId);
    }

    public void checkUsernameExists(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
    }

    // RTR 기법 적용 -> 유효한 RT로 AT 재발급 시 RT도 재발급 대상으로 취급
    // 유효기간이 긴 RT라는 점을 이용해 탈취당한 RT로 계속해서 AT를 재발급 받는 상황 방지
    public AuthInfo reissueToken(HttpServletRequest request) {
        Optional<String> accessToken = RequestTokenExtractor.extractAccessToken(request);
        Optional<String> refreshToken = RequestTokenExtractor.extractRefreshToken(request);

        // refreshToken 검증 및 추출
        String refreshTokenValue = refreshToken
            .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));
        jwtTokenValidator.validateRefreshToken(refreshTokenValue);

        // accessToken이 있으면 블랙리스트 추가
        accessToken.ifPresent(tokenBlacklistService::addAccessTokenToBlacklist);
        tokenBlacklistService.addRefreshTokenToBlacklist(refreshTokenValue);

        Claims claims = refreshTokenProvider.getClaims(refreshTokenValue);
        Long userId = Long.parseLong(claims.getSubject());
        Date refreshExpiration = refreshTokenProvider.getExpiration(refreshTokenValue);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.isDeleted()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String newAccessToken = accessTokenProvider.generateToken(
            user.getUsername(), userId, user.getAuthority()
        );
        String newRefreshToken = refreshTokenProvider.generateReissueToken(
            userId, refreshExpiration
        );

        return new AuthInfo(userId, newAccessToken, newRefreshToken);
    }
}