package com.sparta.tdd.global.config;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AuditorAwareImplTest {

    private final AuditorAwareImpl auditorAware = new AuditorAwareImpl();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuth(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("Authentication이 없는 경우")
    void noAuthentication() {
        //given

        //when
        Optional<Long> auditor = auditorAware.getCurrentAuditor();

        //then
        assertThat(auditor).isEmpty();
    }

    @Test
    @DisplayName("인증되지 않는 Authentication 경우")
    void notAuthenticated(@Mock Authentication auth) {
        //given
        given(auth.isAuthenticated()).willReturn(false);
        setAuth(auth);

        //when
        Optional<Long> auditor = auditorAware.getCurrentAuditor();

        //then
        assertThat(auditor).isEmpty();
        verify(auth).isAuthenticated();
    }

    @Test
    @DisplayName("principal이 익명 사용자인 경우")
    void principalNotUserDetails(@Mock Authentication auth) {
        //given
        given(auth.isAuthenticated()).willReturn(true);
        given(auth.getPrincipal()).willReturn("anonymousUser");
        setAuth(auth);

        //when
        Optional<Long> auditor = auditorAware.getCurrentAuditor();

        //then
        assertThat(auditor).isEmpty();
        verify(auth).isAuthenticated();
        verify(auth).getPrincipal();
    }

    @Test
    @DisplayName("principal이 UserDetailsImpl 타입 인 경우 해당 userId를 반환")
    void principalIsUserDetails(@Mock Authentication auth, @Mock UserDetailsImpl userDetails) {
        //given
        given(auth.isAuthenticated()).willReturn(true);
        given(auth.getPrincipal()).willReturn(userDetails);
        given(userDetails.getUserId()).willReturn(2L);
        setAuth(auth);

        //when
        Optional<Long> auditor = auditorAware.getCurrentAuditor();

        //then
        assertThat(auditor).contains(2L);
        verify(auth).isAuthenticated();
        verify(auth).getPrincipal();
        verify(userDetails, times(1)).getUserId();
    }
}