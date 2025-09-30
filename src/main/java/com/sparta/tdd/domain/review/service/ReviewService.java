package com.sparta.tdd.domain.review.service;

import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.repository.OrderRepository;
import com.sparta.tdd.domain.review.dto.ReviewRequestDto;
import com.sparta.tdd.domain.review.dto.ReviewResponseDto;
import com.sparta.tdd.domain.review.dto.ReviewUpdateDto;
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
        // 엔티티 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 가게입니다."));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        // Review 생성
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
        Review review = reviewRepository.findByIdAndNotDeleted(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        // 본인 확인
        if (!review.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 리뷰만 수정할 수 있습니다.");
        }

        review.updateContent(request.rating(), request.photos(), request.content());

        return ReviewResponseDto.from(review);
    }

    // 리뷰 개별 조회 (답글 포함)
    public ReviewResponseDto getReview(UUID reviewId) {
        Review review = reviewRepository.findByIdAndNotDeleted(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        // 답글 조회
        ReviewReply reply = reviewReplyRepository.findByReviewIdAndNotDeleted(reviewId).orElse(null);
        ReviewResponseDto.ReviewReplyInfo replyInfo = reply != null
                ? new ReviewResponseDto.ReviewReplyInfo(reply.getContent())
                : null;

        return ReviewResponseDto.from(review, replyInfo);
    }

    // 리뷰 목록 조회 (가게별, 답글 포함)
    public Page<ReviewResponseDto> getReviewsByStore(UUID storeId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Review> reviews = reviewRepository.findPageByStoreIdAndNotDeleted(storeId, pageable);

        // 리뷰 ID 목록 추출
        List<UUID> reviewIds = reviews.getContent().stream()
                .map(Review::getId)  // getId() 사용
                .collect(Collectors.toList());

        // 답글 목록 조회
        Map<UUID, ReviewReply> replyMap = reviewReplyRepository.findByReviewIdsAndNotDeleted(reviewIds)
                .stream()
                .collect(Collectors.toMap(ReviewReply::getReviewId, reply -> reply));

        // DTO 변환
        return reviews.map(review -> {
            ReviewReply reply = replyMap.get(review.getId());
            ReviewResponseDto.ReviewReplyInfo replyInfo = reply != null
                    ? new ReviewResponseDto.ReviewReplyInfo(reply.getContent())
                    : null;
            return ReviewResponseDto.from(review, replyInfo);
        });
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