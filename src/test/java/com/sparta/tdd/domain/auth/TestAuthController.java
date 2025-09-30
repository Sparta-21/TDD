package com.sparta.tdd.domain.auth;

import org.springframework.http.ResponseEntity;
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

    public record TokenInfoResponse(
        Long userId,
        String username,
        String authority
    ) {}
}
