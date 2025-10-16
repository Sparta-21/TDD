package com.sparta.tdd.domain.auth.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.sparta.tdd.global.jwt.provider.AccessTokenProvider;
import com.sparta.tdd.global.jwt.provider.RefreshTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class AuthServiceUnitTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AccessTokenProvider accessTokenProvider;
    @Mock
    private RefreshTokenProvider refreshTokenProvider;
    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @Mock
    private WithdrawalDataCleanService withdrawalDataCleanService;
    @Mock
    private JwtTokenValidator jwtTokenValidator;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("회원가입 테스트")
    class SignUpTest {

        // Authority 자동 설정은 통합 테스트에서 검증
        @Test
        @DisplayName("회원가입 성공")
        void signUp_success() {
            // given
            SignUpRequestDto dto = new SignUpRequestDto(
                "user1", "Password1!", "nickname", UserAuthority.CUSTOMER
            );

            User user = User.builder()
                .username("user1")
                .password("encodedPassword")
                .authority(UserAuthority.CUSTOMER)
                .nickname("nickname")
                .build();
            ReflectionTestUtils.setField(user, "id", 1L);

            String accessToken = "accessToken";
            String refreshToken = "refreshToken";

            when(userRepository.existsByUsername("user1")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(accessTokenProvider.generateToken(anyString(), anyLong(), any())).thenReturn(accessToken);
            when(refreshTokenProvider.generateToken(anyString(), anyLong(), any())).thenReturn(refreshToken);

            // when
            AuthInfo result = authService.signUp(dto);

            // then
            assertThat(result.userId()).isEqualTo(user.getId());
            assertThat(result.accessToken()).isEqualTo(accessToken);
            assertThat(result.refreshToken()).isEqualTo(refreshToken);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("회원가입 실패 - 중복 username")
        void signUp_fail_duplicateUsername() {
            // given
            SignUpRequestDto dto = new SignUpRequestDto(
                "user1", "Password1!", "nickname", UserAuthority.CUSTOMER
            );
            when(userRepository.existsByUsername(any())).thenReturn(true);

            // when
            assertThatThrownBy(() -> authService.signUp(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.NICKNAME_ALREADY_EXISTS.getMessage());

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Test
        @DisplayName("로그인 성공")
        void login_success() {
            // given
            LoginRequestDto dto = new LoginRequestDto("user1", "Password1!");

            User user = User.builder()
                .username(dto.username())
                .password("encodedPassword")
                .authority(UserAuthority.CUSTOMER)
                .build();
            ReflectionTestUtils.setField(user, "id", 1L);

            when(userRepository.findByUsername(dto.username())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(dto.password(), user.getPassword())).thenReturn(true);
            when(accessTokenProvider.generateToken(anyString(), anyLong(), any())).thenReturn("accessToken");
            when(refreshTokenProvider.generateToken(anyString(), anyLong(), any())).thenReturn("refreshToken");

            // when
            AuthInfo result = authService.login(dto);

            // then
            assertThat(result.userId()).isEqualTo(1L);
            assertThat(result.accessToken()).isEqualTo("accessToken");
            assertThat(result.refreshToken()).isEqualTo("refreshToken");
        }

        @Test
        @DisplayName("로그인 실패 - 존재하지 않는 사용자")
        void login_fail_userNotFound() {
            // given
            LoginRequestDto requestDto = new LoginRequestDto("user1", "Password123!");

            when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login(requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("로그인 실패 - 비밀번호 불일치")
        void login_fail_passwordMismatch() {
            // given
            LoginRequestDto dto = new LoginRequestDto("user1", "WrongPassword1!");

            User user = User.builder()
                .username("user1")
                .password("encodedPassword")
                .build();

            when(userRepository.findByUsername(dto.username())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(dto.password(), user.getPassword())).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_LOGIN_CREDENTIALS.getMessage());
        }
    }

    @Nested
    @DisplayName("username 중복 체크 테스트")
    class UsernameDuplicateCheckTest {

        @Test
        @DisplayName("username 중복 체크 - 사용 가능")
        void checkUsernameExists_Available() {
            // given
            String username = "user1";
            when(userRepository.existsByUsername("user1")).thenReturn(false);

            // when && then -> 테스트 코드 작성을 위해 메인 코드가 void -> boolean으로 되는 것은 불필요하다고 생각
            // 따라서 assertThat 대신 assertDoesNotThrow로 예외만 발생하지 않는지 확인
            assertDoesNotThrow(() -> authService.checkUsernameExists(username));
        }

        @Test
        @DisplayName("username 중복 체크 - 이미 존재")
        void checkUsernameExists_AlreadyExists() {
            // given
            String username = "existUser";
            when(userRepository.existsByUsername(username)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.checkUsernameExists(username))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.USERNAME_ALREADY_EXISTS.getMessage());
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    class LogoutTest {

        @Test
        @DisplayName("로그아웃 성공 - AT, RT 블랙리스트 추가")
        void login_success() {
            // given
            Cookie refreshCookie = new Cookie("Refresh-Token", "refreshToken");
            when(request.getHeader("Authorization")).thenReturn("Bearer accessToken");
            when(request.getCookies()).thenReturn(new Cookie[]{refreshCookie});

            // when
            authService.logout(request);

            // then
            verify(tokenBlacklistService).addAccessTokenToBlacklist("accessToken");
            verify(tokenBlacklistService).addRefreshTokenToBlacklist("refreshToken");
        }

        @Test
        @DisplayName("로그아웃 성공 - AT만 존재")
        void login_success_withoutRefreshToken() {
            // given
            when(request.getHeader("Authorization")).thenReturn("Bearer accessToken");

            // when
            authService.logout(request);

            // then
            verify(tokenBlacklistService).addAccessTokenToBlacklist("accessToken");
        }
    }

    @Nested
    @DisplayName("회원탈퇴 테스트")
    class WithdrawalTest {

        @Test
        @DisplayName("회원탈퇴 성공 - CUSTOMER (일반 고객)")
        void withdrawal_success_customer() {
            // given
            Long userId = 1L;
            User user = User.builder()
                .username("user1")
                .authority(UserAuthority.CUSTOMER)
                .build();
            ReflectionTestUtils.setField(user, "id", userId);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // when
            authService.withdrawal(userId);

            // then
            verify(reviewRepository).bulkSoftDeleteByUserId(eq(userId), any(), eq(userId));
            verify(withdrawalDataCleanService).deleteCommonUserData(eq(userId), any());
            verify(withdrawalDataCleanService, never()).deleteOwnerRelatedData(any(), any());
        }

        @Test
        @DisplayName("회원탈퇴 성공 - OWNER (가게 사장님)")
        void withdrawal_success_owner() {
            // given
            Long userId = 1L;
            User user = User.builder()
                .username("owner1")
                .authority(UserAuthority.OWNER)
                .build();
            ReflectionTestUtils.setField(user, "id", userId);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // when
            authService.withdrawal(userId);

            // then
            verify(withdrawalDataCleanService).deleteOwnerRelatedData(eq(userId), any());
            verify(withdrawalDataCleanService).deleteCommonUserData(eq(userId), any());
            verify(reviewRepository, never()).bulkSoftDeleteByUserId(any(), any(), any());
        }

        @Test
        @DisplayName("회원탈퇴 실패 - 존재하지 않는 사용자")
        void withdrawal_fail_userNotFound() {
            // given
            Long userId = 999L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.withdrawal(userId))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());

            verify(reviewRepository, never()).bulkSoftDeleteByUserId(any(), any(), any());
            verify(withdrawalDataCleanService, never()).deleteOwnerRelatedData(any(), any());
            verify(withdrawalDataCleanService, never()).deleteCommonUserData(any(), any());
        }
    }

    @Nested
    @DisplayName("토큰 재발급 테스트")
    class reissueTest {

        @Test
        @DisplayName("토큰 재발급 성공 - AT, RT 모두 존재")
        void reissue_success_withAccessToken() {
            // given
            String accessToken = "accessToken";
            String refreshToken = "refreshToken";
            Cookie refreshCookie = new Cookie("Refresh-Token", refreshToken);

            when(request.getHeader("Authorization")).thenReturn("Bearer " + accessToken);
            when(request.getCookies()).thenReturn(new Cookie[]{refreshCookie});

            Claims claims = Jwts.claims()
                .subject("1")
                .build();

            User user = User.builder()
                .username("user1")
                .authority(UserAuthority.CUSTOMER)
                .build();
            ReflectionTestUtils.setField(user, "id", 1L);

            Date refreshExpiration = new Date(System.currentTimeMillis() + 604800000); // 7일 후

            when(refreshTokenProvider.getClaims(refreshToken)).thenReturn(claims);
            when(refreshTokenProvider.getExpiration(refreshToken)).thenReturn(refreshExpiration);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            String newAccessToken = "newAccessToken";
            String newRefreshToken = "newRefreshToken";

            when(accessTokenProvider.generateToken(anyString(), anyLong(), any())).thenReturn(newAccessToken);
            when(refreshTokenProvider.generateReissueToken(anyLong(), any(Date.class))).thenReturn(newRefreshToken);

            // when
            AuthInfo result = authService.reissueToken(request);

            // then
            assertThat(result.accessToken()).isEqualTo(newAccessToken);
            assertThat(result.refreshToken()).isEqualTo(newRefreshToken);
            verify(jwtTokenValidator).validateRefreshToken(refreshToken);
            verify(tokenBlacklistService).addAccessTokenToBlacklist(accessToken);
            verify(tokenBlacklistService).addRefreshTokenToBlacklist(refreshToken);
            verify(userRepository).findById(1L);
        }

        @Test
        @DisplayName("토큰 재발급 성공 - RT만 존재 (AT 없음)")
        void reissue_success_withoutAccessToken() {
            // given
            String refreshToken = "refreshToken";
            Cookie refreshCookie = new Cookie("Refresh-Token", refreshToken);

            when(request.getHeader("Authorization")).thenReturn(null);
            when(request.getCookies()).thenReturn(new Cookie[]{refreshCookie});

            Claims claims = Jwts.claims()
                .subject("1")
                .build();

            User user = User.builder()
                .username("user1")
                .authority(UserAuthority.CUSTOMER)
                .build();
            ReflectionTestUtils.setField(user, "id", 1L);

            Date refreshExpiration = new Date(System.currentTimeMillis() + 604800000);

            when(refreshTokenProvider.getClaims(refreshToken)).thenReturn(claims);
            when(refreshTokenProvider.getExpiration(refreshToken)).thenReturn(refreshExpiration);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            String newAccessToken = "newAccessToken";
            String newRefreshToken = "newRefreshToken";

            when(accessTokenProvider.generateToken(anyString(), anyLong(), any())).thenReturn(newAccessToken);
            when(refreshTokenProvider.generateReissueToken(anyLong(), any(Date.class))).thenReturn(newRefreshToken);

            // when
            AuthInfo result = authService.reissueToken(request);

            // then
            assertThat(result.accessToken()).isEqualTo(newAccessToken);
            assertThat(result.refreshToken()).isEqualTo(newRefreshToken);
            verify(jwtTokenValidator).validateRefreshToken(refreshToken);
            verify(tokenBlacklistService, never()).addAccessTokenToBlacklist(anyString());
            verify(tokenBlacklistService).addRefreshTokenToBlacklist(refreshToken);
            verify(userRepository).findById(1L);
        }

        @Test
        @DisplayName("토큰 재발급 실패 - RT가 이미 블랙리스트인 경우")
        void reissue_fail_refreshToken_alreadyBlacklist() {
            // given
            String refreshToken = "blacklistRefreshToken";
            Cookie refreshCookie = new Cookie("Refresh-Token", refreshToken);
            when(request.getCookies()).thenReturn(new Cookie[]{refreshCookie});

            doThrow(new BusinessException(ErrorCode.REFRESH_TOKEN_BLACKLISTED))
                .when(jwtTokenValidator).validateRefreshToken(refreshToken);

            // when & then
            assertThatThrownBy(() -> authService.reissueToken(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.REFRESH_TOKEN_BLACKLISTED.getMessage());

            verify(tokenBlacklistService, never()).addRefreshTokenToBlacklist(anyString());
        }

        @Test
        @DisplayName("토큰 재발급 실패 - RT가 없는 경우")
        void reissue_fail_refreshToken_notFound() {
            // given - RefreshToken이 쿠키에 없는 상황
            when(request.getCookies()).thenReturn(null);

            // when & then - REFRESH_TOKEN_NOT_FOUND 예외 발생
            assertThatThrownBy(() -> authService.reissueToken(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("토큰 재발급 실패 - 토큰 타입이 refresh가 아님")
        void reissue_fail_refreshToken_typeMismatch() {
            // given
            String refreshToken = "ThisIsNotRefreshToken";
            Cookie refreshCookie = new Cookie("Refresh-Token", refreshToken);
            when(request.getCookies()).thenReturn(new Cookie[]{refreshCookie});

            doThrow(new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID))
                .when(jwtTokenValidator).validateRefreshToken(refreshToken);

            // when & then
            assertThatThrownBy(() -> authService.reissueToken(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.REFRESH_TOKEN_INVALID.getMessage());
        }

        @Test
        @DisplayName("토큰 재발급 실패 - 만료된 RT")
        void reissue_fail_refreshToken_expired() {
            // given
            String refreshToken = "expiredRefreshToken";
            Cookie refreshCookie = new Cookie("Refresh-Token", refreshToken);
            when(request.getCookies()).thenReturn(new Cookie[]{refreshCookie});

            doThrow(new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID))
                .when(jwtTokenValidator).validateRefreshToken(refreshToken);

            // when & then
            assertThatThrownBy(() -> authService.reissueToken(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.REFRESH_TOKEN_INVALID.getMessage());
        }

        @Test
        @DisplayName("토큰 재발급 실패 - 존재하지 않는 사용자")
        void reissue_fail_userNotFound() {
            // given
            String refreshToken = "refreshToken";
            Cookie refreshCookie = new Cookie("Refresh-Token", refreshToken);
            when(request.getCookies()).thenReturn(new Cookie[]{refreshCookie});

            Claims claims = Jwts.claims()
                .subject("999")
                .build();

            when(refreshTokenProvider.getClaims(refreshToken)).thenReturn(claims);
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.reissueToken(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
        }
    }
}
