package com.sparta.tdd.domain.review.controller;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.review.dto.*;
import com.sparta.tdd.domain.review.service.ReviewReplyService;
import com.sparta.tdd.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewReplyService reviewReplyService;

    @PostMapping("/order/{orderId}")
    public ResponseEntity<ReviewResponseDto> createReview(
            @PathVariable UUID orderId,
            @RequestBody @Valid ReviewRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {         ReviewResponseDto response = reviewService.createReview(
                userDetails.getUserId(),
                orderId,
                request
        );
        URI location = URI.create("/v1/reviews/" + response.reviewId());
        return ResponseEntity.created(location).body(response);
    }

    // 리뷰 수정
    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @PathVariable UUID reviewId,
            @RequestBody @Valid ReviewUpdateDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ReviewResponseDto response = reviewService.updateReview(
                reviewId,
                userDetails.getUserId(),
                request
        );
        return ResponseEntity.ok(response);
    }

    // 리뷰 개별 조회
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> getReview(@PathVariable UUID reviewId) {
        ReviewResponseDto response = reviewService.getReview(reviewId);
        return ResponseEntity.ok(response);
    }

    // 리뷰 목록 조회 (가게별)
    @GetMapping("/store/{storeId}")
    public ResponseEntity<Page<ReviewResponseDto>> getReviewsByStore(
            @PathVariable UUID storeId,
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        Page<ReviewResponseDto> reviews = reviewService.getReviewsByStore(storeId, pageable);
        return ResponseEntity.ok(reviews);
    }

    // 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        reviewService.deleteReview(reviewId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    // ========== 답글 관련 엔드포인트 ==========

    @PostMapping("/{reviewId}/reply")
    public ResponseEntity<ReviewReplyResponseDto> createReply(
            @PathVariable UUID reviewId,
            @RequestBody @Valid ReviewReplyRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ReviewReplyResponseDto response = reviewReplyService.createReply(
                reviewId,
                userDetails.getUserId(),
                request
        );

        // 생성된 답글의 위치 (답글은 리뷰의 하위 리소스)
        URI location = URI.create("/v1/reviews/" + reviewId + "/reply");
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{reviewId}/reply")
    public ResponseEntity<ReviewReplyResponseDto> updateReply(
            @PathVariable UUID reviewId,
            @RequestBody @Valid ReviewReplyRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ReviewReplyResponseDto response = reviewReplyService.updateReply(
                reviewId,
                userDetails.getUserId(),
                request
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{reviewId}/reply")
    public ResponseEntity<Void> deleteReply(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        reviewReplyService.deleteReply(reviewId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}