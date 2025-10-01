package com.sparta.tdd.domain.review.service;

import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.repository.OrderRepository;
import com.sparta.tdd.domain.review.dto.*;
import com.sparta.tdd.domain.review.entity.Review;
import com.sparta.tdd.domain.review.entity.ReviewReply;
import com.sparta.tdd.domain.review.repository.ReviewReplyRepository;
import com.sparta.tdd.domain.review.repository.ReviewRepository;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.repository.StoreRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.repository.UserRepository;
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

        Review review = Review.builder()
                .user(user)
                .store(store)
                .order(order)
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
        Review review = findReviewById(reviewId);

        if (!isQualified(review,userId)) {
            throw new IllegalArgumentException("본인의 리뷰만 수정할 수 있습니다.");
        }

        review.updateContent(request.rating(), request.photos(), request.content());

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

        if (!isQualified(review,userId)) {
            throw new IllegalArgumentException("본인의 리뷰만 삭제할 수 있습니다.");
        }

        review.delete(userId);
    }

    private Review findReviewById(UUID reviewId) {
        return reviewRepository.findByIdAndNotDeleted(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));
    }

    // 자격확인 메서드
    private boolean isQualified(Review review,Long userId) {
        return review.getUserId().equals(userId);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    private Store findStoreById(UUID storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 가게입니다."));
    }

    private Order findOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
    }
}