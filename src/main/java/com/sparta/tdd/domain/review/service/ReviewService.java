package com.sparta.tdd.domain.review.service;

import com.sparta.tdd.domain.review.dto.ReviewRequestDto;
import com.sparta.tdd.domain.review.dto.ReviewResponseDto;
import com.sparta.tdd.domain.review.dto.ReviewUpdateDto;
import com.sparta.tdd.domain.review.entity.Review;
import com.sparta.tdd.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;

    // 리뷰 등록
    @Transactional
    public ReviewResponseDto createReview(Long userId, UUID orderId, ReviewRequestDto request) {
        Review review = Review.builder()
                .userId(userId)
                .storeId(request.storeId())
                .orderId(orderId)
                .rating(request.rating())
                .imageUrl(request.photos())
                .content(request.content())
                .build();

        Review savedReview = reviewRepository.save(review);
        return ReviewResponseDto.from(savedReview);
    }

    // 리뷰 수정
    @Transactional
    public ReviewResponseDto updateReview(UUID reviewId, Long userId, ReviewUpdateDto request) {
        Review review = reviewRepository.findByIdAndNotDeleted(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        // 본인 확인
        if (!review.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 리뷰만 수정할 수 있습니다.");
        }

        review.updateContent(request.rating(), request.photos(), request.content());

        return ReviewResponseDto.from(review);
    }

    // 리뷰 개별 조회
    public ReviewResponseDto getReview(UUID reviewId) {
        Review review = reviewRepository.findByIdAndNotDeleted(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        return ReviewResponseDto.from(review);
    }

    // 리뷰 목록 조회 (가게별)
    public Page<ReviewResponseDto> getReviewsByStore(UUID storeId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Review> reviews = reviewRepository.findPageByStoreIdAndNotDeleted(storeId, pageable);

        return reviews.map(ReviewResponseDto::from);
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(UUID reviewId, Long userId) {
        Review review = reviewRepository.findByIdAndNotDeleted(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        // 본인 확인
        if (!review.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 리뷰만 삭제할 수 있습니다.");
        }

        review.delete(userId);
    }
}