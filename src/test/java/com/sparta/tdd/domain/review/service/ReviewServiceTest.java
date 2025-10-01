package com.sparta.tdd.domain.review.service;

import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.enums.OrderStatus;
import com.sparta.tdd.domain.order.repository.OrderRepository;
import com.sparta.tdd.domain.review.dto.ReviewRequestDto;
import com.sparta.tdd.domain.review.dto.ReviewResponseDto;
import com.sparta.tdd.domain.review.dto.ReviewUpdateDto;
import com.sparta.tdd.domain.review.entity.Review;
import com.sparta.tdd.domain.review.entity.ReviewReply;
import com.sparta.tdd.domain.review.repository.ReviewReplyRepository;
import com.sparta.tdd.domain.review.repository.ReviewRepository;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.domain.store.repository.StoreRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService 테스트")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewReplyRepository reviewReplyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User testUser;
    private Store testStore;
    private Order testOrder;
    private Review testReview;
    private UUID reviewId;
    private UUID storeId;
    private UUID orderId;
    private Long userId;
    private Long ownerId;

    @BeforeEach
    void 초기세팅() {
        reviewId = UUID.randomUUID();
        storeId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        testUser = User.builder()
                .username("testuser")
                .password("password123")
                .nickname("테스트유저")
                .authority(UserAuthority.CUSTOMER)
                .build();

        testStore = Store.builder()
                .name("테스트 가게")
                .category(StoreCategory.KOREAN)
                .description("맛있는 한식당")
                .user(testUser)
                .build();

        testOrder = Order.builder()
                .address("서울시 강남구")
                .orderStatus(OrderStatus.DELIVERED)
                .store(testStore)
                .user(testUser)
                .build();

        testReview = Review.builder()
                .user(testUser)
                .store(testStore)
                .order(testOrder)
                .rating(5)
                .imageUrl("http://example.com/image.jpg")
                .content("정말 맛있어요!")
                .build();

        userId = 1L;
        ownerId = 2L;
    }

    @Nested
    @DisplayName("리뷰 등록 테스트")
    class CreateReviewTest {

        @Test
        @DisplayName("리뷰 등록 성공")
        void 리뷰등록_성공() {
            // given
            ReviewRequestDto requestDto = new ReviewRequestDto(
                    "정말 맛있어요!",
                    storeId,
                    5,
                    "http://example.com/image.jpg"
            );

            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(storeRepository.findById(storeId)).willReturn(Optional.of(testStore));
            given(orderRepository.findById(orderId)).willReturn(Optional.of(testOrder));
            given(reviewRepository.save(any(Review.class))).willReturn(testReview);

            // when
            ReviewResponseDto result = reviewService.createReview(userId, orderId, requestDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo("정말 맛있어요!");
            assertThat(result.rating()).isEqualTo(5);

            verify(userRepository).findById(userId);
            verify(storeRepository).findById(storeId);
            verify(orderRepository).findById(orderId);
            verify(reviewRepository).save(any(Review.class));
        }

        @Test
        @DisplayName("리뷰 등록 실패 - 존재하지 않는 사용자")
        void 리뷰등록실패() {
            // given
            ReviewRequestDto requestDto = new ReviewRequestDto(
                    "정말 맛있어요!",
                    storeId,
                    5,
                    "http://example.com/image.jpg"
            );

            // userId가 호출되면 빈 Optional이 반환되도록 설정
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(userId, orderId, requestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 사용자입니다.");

            verify(userRepository).findById(userId);
            verify(reviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("리뷰 등록 실패 - 존재하지 않는 가게")
        void 리뷰등록실패_노가게() {
            // given
            ReviewRequestDto requestDto = new ReviewRequestDto(
                    "정말 맛있어요!",
                    storeId,
                    5,
                    "http://example.com/image.jpg"
            );

            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(storeRepository.findById(storeId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(userId, orderId, requestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 가게입니다.");

            //save가 0번 호풀되고 어떤 실행도 되면 안됌
            verify(reviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("리뷰 등록 실패 - 존재하지 않는 주문")
        void 주문이_존재하지_않을때_리뷰등록_실패() {
            // given
            ReviewRequestDto requestDto = new ReviewRequestDto(
                    "정말 맛있어요!",
                    storeId,
                    5,
                    "http://example.com/image.jpg"
            );

            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(storeRepository.findById(storeId)).willReturn(Optional.of(testStore));
            // 빈 주문 반환
            given(orderRepository.findById(orderId)).willReturn(Optional.empty());

            // when & then
            //주문이 비어있는 상태로 리뷰 등록하려고함
            assertThatThrownBy(() -> reviewService.createReview(userId, orderId, requestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 주문입니다.");

            verify(reviewRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("리뷰 수정 테스트")
    class UpdateReviewTest {

        @Test
        @DisplayName("리뷰 수정 성공")
        void 리뷰_수정_성공() {
            // given
            ReviewUpdateDto updateDto = new ReviewUpdateDto(
                    "수정된 내용",
                    4,
                    "http://example.com/new-image.jpg"
            );

            given(reviewRepository.findByIdAndNotDeleted(reviewId)).willReturn(Optional.of(testReview));

            // when
            ReviewResponseDto result = reviewService.updateReview(reviewId, userId, updateDto);

            // then
            assertThat(result).isNotNull();
            verify(reviewRepository).findByIdAndNotDeleted(reviewId);
        }

        @Test
        @DisplayName("리뷰 수정 실패 - 존재하지 않는 리뷰")
        void 존재하지_않는_리뷰_수정() {
            // given
            ReviewUpdateDto updateDto = new ReviewUpdateDto(
                    "수정된 내용",
                    4,
                    "http://example.com/new-image.jpg"
            );

            //빈 리뷰ID 반환
            given(reviewRepository.findByIdAndNotDeleted(reviewId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.updateReview(reviewId, userId, updateDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 리뷰입니다.");
        }

        @Test
        @DisplayName("리뷰 수정 실패 - 본인의 리뷰가 아님")
        void 내_리뷰아닌데_수정() {
            // given
            ReviewUpdateDto updateDto = new ReviewUpdateDto(
                    "수정된 내용",
                    4,
                    "http://example.com/new-image.jpg"
            );

            given(reviewRepository.findByIdAndNotDeleted(reviewId)).willReturn(Optional.of(testReview));

            // 원래 userId에 1 더해서 다른 유저로 변환
            Long anotherUserId = userId+3;
            // when & then
            assertThatThrownBy(() -> reviewService.updateReview(reviewId, anotherUserId, updateDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("본인의 리뷰만 수정할 수 있습니다.");
        }
    }

    @Nested
    @DisplayName("리뷰 조회 테스트")
    class GetReviewTest {

        @Test
        @DisplayName("리뷰 개별 조회 성공 - 답글 없음")
        void 답글없는리뷰조회() {
            // given
            given(reviewRepository.findByIdAndNotDeleted(reviewId)).willReturn(Optional.of(testReview));
            given(reviewReplyRepository.findByReviewIdAndNotDeleted(reviewId)).willReturn(Optional.empty());

            // when
            ReviewResponseDto result = reviewService.getReview(reviewId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo("정말 맛있어요!");
            assertThat(result.reply()).isNull();

            verify(reviewRepository).findByIdAndNotDeleted(reviewId);
            verify(reviewReplyRepository).findByReviewIdAndNotDeleted(reviewId);
        }

        @Test
        @DisplayName("리뷰 개별 조회 성공 - 답글 포함")
        void 답글있는_리뷰_조회() {
            // given

            ReviewReply reply = ReviewReply.builder()
                    .review(testReview)
                    .content("감사합니다!")
                    .ownerId(ownerId)
                    .build();

            given(reviewRepository.findByIdAndNotDeleted(reviewId)).willReturn(Optional.of(testReview));
            given(reviewReplyRepository.findByReviewIdAndNotDeleted(reviewId)).willReturn(Optional.of(reply));

            // when
            ReviewResponseDto result = reviewService.getReview(reviewId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.reply()).isNotNull();
            assertThat(result.reply().content()).isEqualTo("감사합니다!");
        }

        @Test
        @DisplayName("리뷰 조회 실패 - 존재하지 않는 리뷰")
        void 리뷰조회실패_노존재리뷰() {
            // given
            given(reviewRepository.findByIdAndNotDeleted(reviewId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.getReview(reviewId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 리뷰입니다.");
        }
    }

    @Nested
    @DisplayName("가게별 리뷰 목록 조회 테스트")
    class GetReviewsByStoreTest {

        @Test
        void 리뷰목록조회() {
            // given
            Review review2 = Review.builder()
                    .user(testUser)
                    .store(testStore)
                    .order(testOrder)
                    .rating(4)
                    .content("두 번째 리뷰")
                    .build();

            List<Review> reviewList = Arrays.asList(testReview, review2);
            Pageable pageable = PageRequest.of(0, 10);  // Pageable 객체 생성
            Page<Review> reviewPage = new PageImpl<>(reviewList, pageable, reviewList.size());

            given(reviewRepository.findPageByStoreIdAndNotDeleted(eq(storeId), any(Pageable.class)))
                    .willReturn(reviewPage);
            given(reviewReplyRepository.findByReviewIdsAndNotDeleted(anyList()))
                    .willReturn(Arrays.asList());

            // when
            Page<ReviewResponseDto> result = reviewService.getReviewsByStore(storeId, pageable);  // pageable 추가

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(10);

            verify(reviewRepository).findPageByStoreIdAndNotDeleted(eq(storeId), any(Pageable.class));
            verify(reviewReplyRepository).findByReviewIdsAndNotDeleted(anyList());
        }


        @Test
        @DisplayName("가게별 리뷰 목록 조회 - 빈 결과")
        void 빈리뷰조회() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Review> emptyPage = Page.empty(pageable);  // 👈 더 간단한 방법

            given(reviewRepository.findPageByStoreIdAndNotDeleted(eq(storeId), any(Pageable.class)))
                    .willReturn(emptyPage);

            // when
            Page<ReviewResponseDto> result = reviewService.getReviewsByStore(storeId, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("리뷰 삭제 테스트")
    class DeleteReviewTest {

        @Test
        @DisplayName("리뷰 삭제 성공")
        void 리뷰삭제성공() {
            // given
            given(reviewRepository.findByIdAndNotDeleted(reviewId)).willReturn(Optional.of(testReview));

            // when
            reviewService.deleteReview(reviewId, userId);

            // then
            verify(reviewRepository).findByIdAndNotDeleted(reviewId);
        }

        @Test
        @DisplayName("리뷰 삭제 실패 - 존재하지 않는 리뷰")
        void 리뷰삭제실패_리뷰가없음() {
            // given
            given(reviewRepository.findByIdAndNotDeleted(reviewId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.deleteReview(reviewId, userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 리뷰입니다.");
        }

        @Test
        @DisplayName("리뷰 삭제 실패 - 본인의 리뷰가 아님")
        void 내리뷰아닌데삭제() {
            // given
            given(reviewRepository.findByIdAndNotDeleted(reviewId)).willReturn(Optional.of(testReview));

            // when & then
            assertThatThrownBy(() -> reviewService.deleteReview(reviewId, 999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("본인의 리뷰만 삭제할 수 있습니다.");
        }
    }
}
