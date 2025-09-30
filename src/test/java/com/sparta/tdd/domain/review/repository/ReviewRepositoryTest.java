package com.sparta.tdd.domain.review.repository;

import com.sparta.tdd.domain.review.entity.Review;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@EnableJpaAuditing
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    @DisplayName("ID로 리뷰를 조회할 수 있다")
    void 리뷰조회() {
        // given
        Review review = Review.builder()
                .userId(1L)
                .storeId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .rating(5)
                .imageUrl("이미지")
                .content("맛있어요")
                .build();
        Review savedReview = reviewRepository.save(review);

        // when
        Optional<Review> foundReview = reviewRepository.findById(savedReview.getReviewId());

        // then
        assertThat(foundReview).isPresent();
        assertThat(foundReview.get().getContent()).isEqualTo("맛있어요");
    }

    @Test
    @DisplayName("삭제되지 않은 ID로 리뷰를 조회할 수 있다")
    void 삭제되지않은리뷰조회2() {
        // given
        Review review = Review.builder()
                .userId(1L)
                .storeId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .rating(5)
                .imageUrl("이미지")
                .content("맛있어요")
                .build();
        Review savedReview = reviewRepository.save(review);

        // when
        Optional<Review> foundReview = reviewRepository.findByIdAndNotDeleted(savedReview.getReviewId());

        // then
        assertThat(foundReview).isPresent();
        assertThat(foundReview.get().getContent()).isEqualTo("맛있어요");
    }

    @Test
    @DisplayName("리뷰를 수정할 수 있다")
    void 리뷰수정() {
        // given
        Review review = Review.builder()
                .userId(1L)
                .storeId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .rating(3)
                .imageUrl("이미지")
                .content("보통이에요")
                .build();
        Review savedReview = reviewRepository.save(review);

        // when
        savedReview.updateContent(5, "주소변경", "수정되나 이거");
        Review updatedReview = reviewRepository.save(savedReview);

        // then
        assertThat(updatedReview.getRating()).isEqualTo(5);
        assertThat(updatedReview.getContent()).isEqualTo("수정되나 이거");
        assertThat(updatedReview.getImageUrl()).isEqualTo("주소변경");
    }

    @Test
    @DisplayName("리뷰를 삭제할 수 있다")
    void 리뷰삭제() {
        // given
        Review review = Review.builder()
                .userId(1L)
                .storeId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .rating(4)
                .content("좋아요")
                .build();
        Review savedReview = reviewRepository.save(review);

        // when
        savedReview.delete(1L);
        reviewRepository.save(savedReview);

        // then
        Review deletedReview = reviewRepository.findById(savedReview.getReviewId()).get();
        assertThat(deletedReview.isDeleted()).isTrue();
        assertThat(deletedReview.getDeletedAt()).isNotNull();
        assertThat(deletedReview.getDeletedBy()).isEqualTo(1L);
    }

    @Test
    @DisplayName("특정 가게의 리뷰 목록을 조회할 수 있다")
    void 리뷰목록조회_가게() {
        // given
        UUID storeId = UUID.randomUUID();

        Review review1 = Review.builder()
                .userId(1L)
                .storeId(storeId)
                .orderId(UUID.randomUUID())
                .rating(5)
                .content("맛있어요")
                .build();

        Review review2 = Review.builder()
                .userId(2L)
                .storeId(storeId)
                .orderId(UUID.randomUUID())
                .rating(4)
                .content("좋아요")
                .build();

        Review review3 = Review.builder()
                .userId(3L)
                .storeId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .rating(3)
                .content("다른 가게")
                .build();

        reviewRepository.save(review1);
        reviewRepository.save(review2);
        reviewRepository.save(review3);

        // when
        List<Review> storeReviews = reviewRepository.findByStoreIdAndNotDeleted(storeId);

        // then
        assertThat(storeReviews).hasSize(2);
    }

    @Test
    @DisplayName("특정 유저의 리뷰 목록을 조회할 수 있다")
    void 리뷰목록조회_개인() {
        // given
        Long userId = 1L;
        UUID storeId1 = UUID.randomUUID();
        UUID storeId2 = UUID.randomUUID();

        Review review1 = Review.builder()
                .userId(userId)
                .storeId(storeId1)
                .orderId(UUID.randomUUID())
                .rating(5)
                .content("맛있어요")
                .build();

        Review review2 = Review.builder()
                .userId(userId)
                .storeId(storeId2)
                .orderId(UUID.randomUUID())
                .rating(4)
                .content("좋아요")
                .build();

        Review review3 = Review.builder()
                .userId(2L)
                .storeId(storeId1)
                .orderId(UUID.randomUUID())
                .rating(3)
                .content("보통이에요")
                .build();

        reviewRepository.save(review1);
        reviewRepository.save(review2);
        reviewRepository.save(review3);

        // when
        List<Review> userReviews = reviewRepository.findByUserIdAndNotDeleted(userId);

        // then
        assertThat(userReviews).hasSize(2);
    }

    @Test
    @DisplayName("모든 리뷰 확인 매니저 이상 급")
    void 리뷰목록조회_전체() {
        // given
        Review review1 = Review.builder()
                .userId(1L)
                .storeId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .rating(5)
                .content("맛있어요")
                .build();

        Review review2 = Review.builder()
                .userId(3L)
                .storeId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .rating(4)
                .content("좋아요")
                .build();

        Review review3 = Review.builder()
                .userId(2L)
                .storeId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .rating(3)
                .content("보통이에요")
                .build();

        reviewRepository.save(review1);
        reviewRepository.save(review2);
        reviewRepository.save(review3);

        // when
        List<Review> userReviews = reviewRepository.findAllNotDeleted();

        // then
        assertThat(userReviews).hasSize(3);
    }

    @Test
    @DisplayName("특정 가게의 평점을 확인할 수 있다.")
    void 평점확인() {
        // given
        UUID storeId1 = UUID.randomUUID();

        Review review1 = Review.builder()
                .userId(1L)
                .storeId(storeId1)
                .orderId(UUID.randomUUID())
                .rating(5)
                .content("맛있어요")
                .build();

        Review review2 = Review.builder()
                .userId(1L)
                .storeId(storeId1)
                .orderId(UUID.randomUUID())
                .rating(4)
                .content("좋아요")
                .build();

        Review review3 = Review.builder()
                .userId(1L)
                .storeId(storeId1)
                .orderId(UUID.randomUUID())
                .rating(3)
                .content("보통이에요")
                .build();

        reviewRepository.save(review1);
        reviewRepository.save(review2);
        reviewRepository.save(review3);

        // when
        Double average = reviewRepository.findAverageRatingByStoreId(storeId1);

        // then
        assertThat(average).isEqualTo(4.0);
    }
}