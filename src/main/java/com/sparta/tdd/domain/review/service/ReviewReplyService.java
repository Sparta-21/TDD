package com.sparta.tdd.domain.review.service;

import com.sparta.tdd.domain.review.dto.ReviewReplyRequestDto;
import com.sparta.tdd.domain.review.dto.ReviewReplyResponseDto;
import com.sparta.tdd.domain.review.entity.Review;
import com.sparta.tdd.domain.review.entity.ReviewReply;
import com.sparta.tdd.domain.review.repository.ReviewReplyRepository;
import com.sparta.tdd.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewReplyService {

    private final ReviewRepository reviewRepository;
    private final ReviewReplyRepository reviewReplyRepository;

    // 답글 등록
    @Transactional
    public ReviewReplyResponseDto createReply(UUID reviewId, Long ownerId, ReviewReplyRequestDto request) {
        Review review = findReviewById(reviewId);

        // 이미 답글이 있는지 확인
        reviewReplyRepository.findByReviewIdAndNotDeleted(reviewId)
                .ifPresent(reply -> {
                    throw new IllegalArgumentException("이미 답글이 존재합니다.");
                });

        // 가게 소유자 확인 (필요시 Store 엔티티에서 ownerId 확인 로직 추가)
        // Store store = review.getStore();
        // if (!store.getOwnerId().equals(ownerId)) {
        //     throw new IllegalArgumentException("해당 가게의 소유자만 답글을 작성할 수 있습니다.");
        // }

        ReviewReply reply = ReviewReply.builder()
                .review(review)
                .content(request.content())
                .ownerId(ownerId)
                .build();

        ReviewReply savedReply = reviewReplyRepository.save(reply);
        return ReviewReplyResponseDto.from(savedReply);
    }

    // 답글 수정
    @Transactional
    public ReviewReplyResponseDto updateReply(UUID reviewId, Long ownerId, ReviewReplyRequestDto request) {
        ReviewReply reply = findReplyById(reviewId);

        if (!reply.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("본인의 답글만 수정할 수 있습니다.");
        }

        reply.updateContent(request.content());

        return ReviewReplyResponseDto.from(reply);
    }

    // 답글 삭제
    @Transactional
    public void deleteReply(UUID reviewId, Long ownerId) {
        ReviewReply reply = findReplyById(reviewId);

        if (!reply.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("본인의 답글만 삭제할 수 있습니다.");
        }

        reply.delete(ownerId);
    }

    // ========== Private Helper 메서드 ==========

    /**
     * 리뷰 ID로 삭제되지 않은 리뷰 조회
     */
    private Review findReviewById(UUID reviewId) {
        return reviewRepository.findByIdAndNotDeleted(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));
    }

    /**
     * 리뷰 ID로 삭제되지 않은 답글 조회
     */
    private ReviewReply findReplyById(UUID reviewId) {
        return reviewReplyRepository.findByReviewIdAndNotDeleted(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 답글입니다."));
    }
}