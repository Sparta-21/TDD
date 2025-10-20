package com.sparta.tdd.domain.review.controller;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.review.dto.*;
import com.sparta.tdd.domain.review.dto.request.ReviewReplyRequestDto;
import com.sparta.tdd.domain.review.dto.request.ReviewRequestDto;
import com.sparta.tdd.domain.review.dto.response.ReviewReplyResponseDto;
import com.sparta.tdd.domain.review.dto.response.ReviewResponseDto;
import com.sparta.tdd.domain.review.service.ReviewReplyService;
import com.sparta.tdd.domain.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@Tag(name = "리뷰 API")
@RestController
@RequestMapping("/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewReplyService reviewReplyService;

    @Operation(
            summary = "리뷰 생성",
            description = """
            주문 완료 후 고객이 가게에 대한 리뷰를 작성합니다.\n
            orderId, storeId, rating(1~5점), content, photos를 받아 리뷰를 생성합니다.\n
            리뷰 생성 시 가게의 평균 평점이 자동으로 재계산됩니다.\n
            생성된 리뷰의 위치(Location 헤더)와 함께 201 Created 상태로 응답합니다.\n
            인증된 사용자만 접근 가능합니다.
            """
    )
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
    @Operation(
            summary = "리뷰 수정",
            description = """
            작성한 리뷰의 내용(content), 평점(rating), 사진(photos)을 수정합니다.\n
            본인이 작성한 리뷰만 수정할 수 있으며, 수정 시 가게의 평균 평점이 재계산됩니다.\n
            수정된 리뷰 정보를 200 OK 상태로 응답합니다.\n
            삭제되지 않은 리뷰만 수정 가능합니다.
            """
    )
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
    @Operation(
            summary = "리뷰 개별 조회",
            description = """
            특정 리뷰의 상세 정보를 조회합니다.\n
            리뷰 정보와 함께 답글이 있는 경우 답글 정보도 함께 반환됩니다.\n
            삭제되지 않은 리뷰만 조회 가능합니다.\n
            인증 없이 모든 사용자가 조회할 수 있습니다.
            """
    )
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> getReview(@PathVariable UUID reviewId) {
        ReviewResponseDto response = reviewService.getReview(reviewId);
        return ResponseEntity.ok(response);
    }

    // 리뷰 목록 조회 (가게별)
    @Operation(
            summary = "가게별 리뷰 목록 조회",
            description = """
            특정 가게의 모든 리뷰를 페이징하여 조회합니다.\n
            각 리뷰에 답글이 있는 경우 답글 정보도 함께 반환됩니다.\n
            삭제되지 않은 리뷰만 조회되며, 페이지네이션을 통해 대량의 리뷰를 효율적으로 조회할 수 있습니다.\n
            인증 없이 모든 사용자가 조회할 수 있습니다.
            """
    )
    @GetMapping("/store/{storeId}")
    public ResponseEntity<Page<ReviewResponseDto>> getReviewsByStore(
            @PathVariable UUID storeId,
            @PageableDefault Pageable pageable
    ) {
        Page<ReviewResponseDto> reviews = reviewService.getReviewsByStore(storeId, pageable);
        return ResponseEntity.ok(reviews);
    }

    // 리뷰 삭제
    @Operation(
            summary = "리뷰 삭제",
            description = """
            작성한 리뷰를 논리적으로 삭제합니다.\n
            본인이 작성한 리뷰만 삭제할 수 있으며, deletedAt과 deletedBy 필드가 설정됩니다.\n
            리뷰 삭제 시 가게의 평균 평점이 자동으로 재계산됩니다.\n
            삭제 성공 시 204 No Content 상태로 응답합니다.\n
            물리적 삭제가 아닌 논리적 삭제(Soft Delete)로 데이터는 DB에 유지됩니다.
            """
    )
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        reviewService.deleteReview(reviewId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    // ========== 답글 관련 엔드포인트 ==========
    @Operation(
            summary = "리뷰 답글 작성",
            description = """
            가게 사장님이 리뷰에 대한 답글을 작성합니다.\n
            OWNER 권한이 필요하며, 본인 가게의 리뷰에만 답글을 작성할 수 있습니다.\n
            하나의 리뷰에는 하나의 답글만 작성 가능합니다.\n
            이미 답글이 존재하는 경우 에러가 발생합니다.\n
            생성된 답글의 위치(Location 헤더)와 함께 201 Created 상태로 응답합니다.
            """
    )
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
    @Operation(
            summary = "리뷰 답글 수정",
            description = """
            작성한 답글의 내용을 수정합니다.\n
            OWNER 권한이 필요하며, 본인 가게의 리뷰 답글만 수정할 수 있습니다.\n
            삭제되지 않은 답글만 수정 가능합니다.\n
            수정된 답글 정보를 200 OK 상태로 응답합니다.
            """
    )
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

    @Operation(
            summary = "리뷰 답글 삭제",
            description = """
            작성한 답글을 논리적으로 삭제합니다.\n
            OWNER, MANAGER, MASTER 권한이 필요하며, 본인 가게의 리뷰 답글만 삭제할 수 있습니다.\n
            deletedAt과 deletedBy 필드가 설정되며, 물리적 삭제가 아닌 논리적 삭제(Soft Delete)입니다.\n
            삭제 성공 시 204 No Content 상태로 응답합니다.
            """
    )
    @DeleteMapping("/{reviewId}/reply")
    public ResponseEntity<Void> deleteReply(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        reviewReplyService.deleteReply(reviewId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}