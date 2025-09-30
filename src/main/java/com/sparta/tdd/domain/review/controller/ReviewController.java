package com.sparta.tdd.domain.review.controller;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.review.dto.*;
import com.sparta.tdd.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{orderId}")
    public ResponseEntity<ReviewResponseDto> createReview(
            @PathVariable UUID orderId,
            @RequestBody @Valid ReviewRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ReviewResponseDto response = reviewService.createReview(
                userDetails.getUserId(),
                orderId,
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
    public ResponseEntity<Map<String, Object>> getReviewsByStore(
            @PathVariable UUID storeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ReviewResponseDto> reviews = reviewService.getReviewsByStore(storeId, page, size);

        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("page", reviews.getNumber() + 1);
        pageInfo.put("size", reviews.getSize());
        pageInfo.put("totalElements", reviews.getTotalElements());
        pageInfo.put("totalPages", reviews.getTotalPages());
        pageInfo.put("hasNext", reviews.hasNext());

        Map<String, Object> result = new HashMap<>();
        result.put("status", 200);
        result.put("reviews", reviews.getContent());
        result.put("pageInfo", pageInfo);

        return ResponseEntity.ok(result);
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
        ReviewReplyResponseDto response = reviewService.createReply(
                reviewId,
                userDetails.getUserId(),
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{reviewId}/reply")
    public ResponseEntity<ReviewReplyResponseDto> updateReply(
            @PathVariable UUID reviewId,
            @RequestBody @Valid ReviewReplyRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ReviewReplyResponseDto response = reviewService.updateReply(
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
        reviewService.deleteReply(reviewId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}