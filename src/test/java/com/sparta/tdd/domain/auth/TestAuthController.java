package com.sparta.tdd.domain.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/test")
public class TestAuthController {

    @GetMapping("/token/info")
    public ResponseEntity<?> getUserDetailsInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("인증되지 않은 사용자");
        }

        return ResponseEntity.ok(new TokenInfoResponse(
            userDetails.getUserId(),
            userDetails.getUsername(),
            userDetails.getUserAuthority().name()
        ));
    }

    @GetMapping("/mockUser/test")
    public ResponseEntity<?> mockUserTest(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("인증되지 않은 사용자");
        }

        return ResponseEntity.ok(new mockUserInfo(
            userDetails.getUserId(),
            userDetails.getUsername(),
            userDetails.getUserAuthority().name()
        ));
    }

    @PreAuthorize("hasRole('MASTER')")
    @GetMapping("/master-only")
    public ResponseEntity<?> masterOnlyAccessTest(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(new mockUserInfo(
            userDetails.getUserId(),
            userDetails.getUsername(),
            userDetails.getUserAuthority().name()
        ));
    }

    public record TokenInfoResponse(
        Long userId,
        String username,
        String authority
    ) {

    }

    public record mockUserInfo(
        Long userId,
        String username,
        String authority
    ) {

    }
}
