package com.sparta.tdd.domain.review.service;

import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.repository.OrderRepository;
import com.sparta.tdd.domain.review.dto.*;
import com.sparta.tdd.domain.review.dto.request.ReviewRequestDto;
import com.sparta.tdd.domain.review.dto.response.ReviewResponseDto;
import com.sparta.tdd.domain.review.entity.Review;
import com.sparta.tdd.domain.review.entity.ReviewReply;
import com.sparta.tdd.domain.review.repository.ReviewReplyRepository;
import com.sparta.tdd.domain.review.repository.ReviewRepository;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.repository.StoreRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.repository.UserRepository;
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;

    // 리뷰 등록
    @Transactional
    public ReviewResponseDto createReview(Long userId, UUID orderId, ReviewRequestDto request) {
        User user = findUserById(userId);
        Store store = findStoreById(request.storeId());
        Order order = findOrderById(orderId);

        existsByOrderId(orderId);

        Review review = request.toEntity(user, store, order);

        Review savedReview = reviewRepository.save(review);

        updateStoreRating(store);
        return ReviewResponseDto.from(savedReview);
    }

    // 리뷰 수정
    @Transactional
    public ReviewResponseDto updateReview(UUID reviewId, Long userId, ReviewUpdateDto request) {
        Review review = findReviewById(reviewId);

        if (!isQualified(review,userId)) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_OWNED);
        }

        review.updateContent(request.rating(), request.photos(), request.content());

        Store store = review.getStore();
        updateStoreRating(store);

        return ReviewResponseDto.from(review);
    }

    public ReviewResponseDto getReview(UUID reviewId) {
        Review review = findReviewById(reviewId);

        // 아까 피드백 받긴 했지만 orElse(null) -> 답글이 없을수도 있기 때문에 orElse(null)을 사용하겠습니다.
        // 만약 답글이 무조건 있어야한다면 아까 튜터님이 말한대로 orElseThrow로 수정하겠습니다.
        ReviewReply reply = reviewReplyRepository.findByReviewIdAndNotDeleted(reviewId).orElse(null);
        if (reply == null) {
            return ReviewResponseDto.from(review, null);
        }

        ReviewResponseDto.ReviewReplyInfo replyInfo =
                new ReviewResponseDto.ReviewReplyInfo(reply.getContent());
        return ReviewResponseDto.from(review, replyInfo);
    }

    // 리뷰 목록 조회 (가게별, 답글 포함)
    public Page<ReviewResponseDto> getReviewsByStore(UUID storeId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findPageByStoreIdAndNotDeleted(storeId, pageable);

        List<UUID> reviewIds = reviews.getContent().stream()
                .map(Review::getId)
                .toList();

        Map<UUID, ReviewReply> replyMap = reviewReplyRepository.findByReviewIdsAndNotDeleted(reviewIds)
                .stream()
                .collect(Collectors.toMap(ReviewReply::getReviewId, reply -> reply));

        return reviews.map(review -> {
            ReviewReply reply = replyMap.get(review.getId());

            if (reply != null) {
                ReviewResponseDto.ReviewReplyInfo replyInfo =
                        new ReviewResponseDto.ReviewReplyInfo(reply.getContent());
                return ReviewResponseDto.from(review, replyInfo);
            }

            return ReviewResponseDto.from(review, null);
        });
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(UUID reviewId, Long userId) {
        Review review = findReviewById(reviewId);

        if (!isQualified(review, userId)) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_OWNED);
        }

        review.delete(userId);
        Store store = review.getStore();
        updateStoreRating(store);
    }

    // 평점 계산 및 업데이트 메서드
    private void updateStoreRating(Store store) {
        Double avgRating = reviewRepository.findAverageRatingByStoreId(store.getId());
        Long reviewCount = reviewRepository.countByStoreIdAndNotDeleted(store.getId());

        store.updateRatingInfo(
                avgRating != null ? BigDecimal.valueOf(avgRating) : BigDecimal.ZERO,
                reviewCount.intValue()
        );
    }

    private Review findReviewById(UUID reviewId) {
        return reviewRepository.findByIdAndNotDeleted(reviewId)
            .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
    }

    // 자격확인 메서드
    private boolean isQualified(Review review, Long userId) {
        return review.getUserId().equals(userId);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Store findStoreById(UUID storeId) {
        return storeRepository.findById(storeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
    }

    private Order findOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    private void existsByOrderId(UUID orderId) {
        if (reviewRepository.existsByOrderId(orderId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_REVIEW);
        }
    }
}