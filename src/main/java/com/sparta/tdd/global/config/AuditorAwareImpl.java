package com.sparta.tdd.global.config;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        // SecurityContextHolder 에서 현재 인증 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 없거나 Optional.empty()를 반환
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        // principal이 우리가 정의한 UserDetailsImpl 타입이 아닐 경우 Optional.empty() 반환
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            return Optional.empty();
        }

        // UserDetailsImpl 에서 사용자 ID를 반환
        return Optional.of(userDetails.getUserId());
    }
}