package com.sparta.tdd.domain.review.service;

import com.sparta.tdd.domain.review.dto.ReviewReplyRequestDto;
import com.sparta.tdd.domain.review.dto.ReviewReplyResponseDto;
import com.sparta.tdd.domain.review.entity.Review;
import com.sparta.tdd.domain.review.entity.ReviewReply;
import com.sparta.tdd.domain.review.repository.ReviewReplyRepository;
import com.sparta.tdd.domain.review.repository.ReviewRepository;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.repository.StoreRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewReplyService {

    private final ReviewRepository reviewRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    // 답글 등록
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @Transactional
    public ReviewReplyResponseDto createReply(UUID reviewId, Long ownerId, ReviewReplyRequestDto request) {
        Review review = findReviewById(reviewId);

        // 가게 소유자 확인
        validateStoreOwner(review.getStoreId(), ownerId);
        // 이미 답글이 있는지 확인
        checkReplyExists(reviewId);
        ReviewReply reply = request.toEntity(review, ownerId);
        ReviewReply savedReply = reviewReplyRepository.save(reply);
        return ReviewReplyResponseDto.from(savedReply);
    }

    // 답글 수정
    @PreAuthorize("hasAnyRole('OWNER')")
    @Transactional
    public ReviewReplyResponseDto updateReply(UUID reviewId, Long ownerId, ReviewReplyRequestDto request) {
        ReviewReply reply = findReplyById(reviewId);
        validateStoreOwner(reply.getReview().getStoreId(), ownerId);
        reply.updateContent(request.content());
        return ReviewReplyResponseDto.from(reply);
    }

    // 답글 삭제
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @Transactional
    public void deleteReply(UUID reviewId, Long ownerId) {
        ReviewReply reply = findReplyById(reviewId);
        validateStoreOwner(reply.getReview().getStoreId(), ownerId);
        reply.delete(ownerId);
    }

    // 이미 답글이 있는지 확인
    private void checkReplyExists(UUID reviewId) {
        reviewReplyRepository.findByReviewIdAndNotDeleted(reviewId)
                .ifPresent(reply -> {
                    throw new IllegalArgumentException("이미 답글이 존재합니다.");
                });
    }

    // 리뷰 ID로 삭제되지 않은 리뷰 조회
    private Review findReviewById(UUID reviewId) {
        return reviewRepository.findByIdAndNotDeleted(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));
    }

    //리뷰 ID로 삭제되지 않은 답글 조회
    private ReviewReply findReplyById(UUID reviewId) {
        return reviewReplyRepository.findByReviewIdAndNotDeleted(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 답글입니다."));
    }

    //가게 소유자 검증
    private void validateStoreOwner(UUID storeId, Long userId) {
        Store store = storeRepository.findByStoreIdAndNotDeleted(storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 가게입니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // MANAGER나 MASTER면 통과, OWNER면 가게 소유자 확인
        if (user.getAuthority() == UserAuthority.MANAGER || user.getAuthority() == UserAuthority.MASTER) {
            return;
        }

        if (!store.isOwner(user)) {
            throw new IllegalArgumentException("해당 가게의 소유자만 답글을 작성할 수 있습니다.");
        }
    }
}