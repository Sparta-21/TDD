package com.sparta.tdd.domain.review.service;

import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.enums.OrderStatus;
import com.sparta.tdd.domain.review.dto.ReviewReplyRequestDto;
import com.sparta.tdd.domain.review.dto.ReviewReplyResponseDto;
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

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewReplyService 테스트")
class ReviewReplyServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewReplyRepository reviewReplyRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewReplyService reviewReplyService;

    private User customer;
    private User owner;
    private Store testStore;
    private Order testOrder;
    private Review testReview;
    private ReviewReply testReply;
    private UUID reviewId;
    private UUID storeId;

    @BeforeEach
    void setUp() throws Exception {
        reviewId = UUID.randomUUID();
        storeId = UUID.randomUUID();

        // 고객 생성
        customer = User.builder()
                .username("customer")
                .password("password123")
                .nickname("고객")
                .authority(UserAuthority.CUSTOMER)
                .build();
        setUserId(customer, 1L);

        // 가게 사장 생성
        owner = User.builder()
                .username("owner")
                .password("password123")
                .nickname("사장님")
                .authority(UserAuthority.OWNER)
                .build();
        setUserId(owner, 2L);

        // 가게 생성
        testStore = Store.builder()
                .name("테스트 가게")
                .category(StoreCategory.KOREAN)
                .description("맛있는 한식당")
                .user(owner)
                .build();
        setStoreId(testStore, storeId);

        // 주문 생성
        testOrder = Order.builder()
                .address("서울시 강남구")
                .orderStatus(OrderStatus.DELIVERED)
                .store(testStore)
                .user(customer)
                .build();

        // 리뷰 생성
        testReview = Review.builder()
                .user(customer)
                .store(testStore)
                .order(testOrder)
                .rating(5)
                .imageUrl("http://example.com/image.jpg")
                .content("정말 맛있어요!")
                .build();
        setReviewId(testReview, reviewId);

        // 답글 생성
        testReply = ReviewReply.builder()
                .review(testReview)
                .content("감사합니다!")
                .ownerId(owner.getId())
                .build();
    }

    // Reflection을 사용하여 ID 설정 (테스트용)
    private void setUserId(User user, Long id) throws Exception {
        Field field = User.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(user, id);
    }

    private void setStoreId(Store store, UUID id) throws Exception {
        Field field = Store.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(store, id);
    }

    private void setReviewId(Review review, UUID id) throws Exception {
        Field field = Review.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(review, id);
    }

    @Nested
    @DisplayName("답글 등록 테스트")
    class CreateReplyTest {

        @Test
        @DisplayName("답글 등록 성공")
        void createReply_Success() {
            // given
            ReviewReplyRequestDto requestDto = new ReviewReplyRequestDto("감사합니다!");

            given(reviewRepository.findByIdAndNotDeleted(reviewId)).willReturn(Optional.of(testReview));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(testStore));
            given(userRepository.findById(owner.getId())).willReturn(Optional.of(owner));
            given(reviewReplyRepository.findByReviewIdAndNotDeleted(reviewId)).willReturn(Optional.empty());
            given(reviewReplyRepository.save(any(ReviewReply.class))).willReturn(testReply);

            // when
            ReviewReplyResponseDto result = reviewReplyService.createReply(reviewId, owner.getId(), requestDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo("감사합니다!");
            assertThat(result.ownerId()).isEqualTo(owner.getId());

            verify(reviewRepository).findByIdAndNotDeleted(reviewId);
            verify(storeRepository).findByStoreIdAndNotDeleted(storeId);
            verify(userRepository).findById(owner.getId());
            verify(reviewReplyRepository).findByReviewIdAndNotDeleted(reviewId);
            verify(reviewReplyRepository).save(any(ReviewReply.class));
        }

        @Test
        @DisplayName("답글 등록 실패 - 존재하지 않는 리뷰")
        void createReply_Fail_ReviewNotFound() {
            // given
            ReviewReplyRequestDto requestDto = new ReviewReplyRequestDto("감사합니다!");

            given(reviewRepository.findByIdAndNotDeleted(reviewId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewReplyService.createReply(reviewId, owner.getId(), requestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 리뷰입니다.");

            verify(reviewReplyRepository, never()).save(any());
        }

        @Test
        @DisplayName("답글 등록 실패 - 이미 답글 존재")
        void createReply_Fail_ReplyAlreadyExists() {
            // given
            ReviewReplyRequestDto requestDto = new ReviewReplyRequestDto("감사합니다!");

            given(reviewRepository.findByIdAndNotDeleted(reviewId)).willReturn(Optional.of(testReview));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(testStore));
            given(userRepository.findById(owner.getId())).willReturn(Optional.of(owner));
            given(reviewReplyRepository.findByReviewIdAndNotDeleted(reviewId)).willReturn(Optional.of(testReply));

            // when & then
            assertThatThrownBy(() -> reviewReplyService.createReply(reviewId, owner.getId(), requestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 답글이 존재합니다.");

            verify(reviewReplyRepository, never()).save(any());
        }

        @Test
        @DisplayName("답글 등록 실패 - 가게 소유자가 아님")
        void createReply_Fail_NotStoreOwner() {
            // given
            ReviewReplyRequestDto requestDto = new ReviewReplyRequestDto("감사합니다!");
            User otherUser = User.builder()
                    .username("other")
                    .password("password123")
                    .nickname("다른사람")
                    .authority(UserAuthority.OWNER)
                    .build();

            given(reviewRepository.findByIdAndNotDeleted(reviewId)).willReturn(Optional.of(testReview));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(testStore));
            given(userRepository.findById(3L)).willReturn(Optional.of(otherUser));

            // when & then
            assertThatThrownBy(() -> reviewReplyService.createReply(reviewId, 3L, requestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("해당 가게의 소유자만 답글을 작성할 수 있습니다.");

            verify(reviewReplyRepository, never()).save(any());
        }

        @Test
        @DisplayName("답글 등록 실패 - 존재하지 않는 가게")
        void createReply_Fail_StoreNotFound() {
            // given
            ReviewReplyRequestDto requestDto = new ReviewReplyRequestDto("감사합니다!");

            given(reviewRepository.findByIdAndNotDeleted(reviewId)).willReturn(Optional.of(testReview));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewReplyService.createReply(reviewId, owner.getId(), requestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 가게입니다.");

            verify(reviewReplyRepository, never()).save(any());
        }

        @Test
        @DisplayName("답글 등록 실패 - 존재하지 않는 사용자")
        void createReply_Fail_UserNotFound() {
            // given
            ReviewReplyRequestDto requestDto = new ReviewReplyRequestDto("감사합니다!");

            given(reviewRepository.findByIdAndNotDeleted(reviewId)).willReturn(Optional.of(testReview));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(testStore));
            given(userRepository.findById(owner.getId())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewReplyService.createReply(reviewId, owner.getId(), requestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 사용자입니다.");

            verify(reviewReplyRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("답글 수정 테스트")
    class UpdateReplyTest {

        @Test
        @DisplayName("답글 수정 성공")
        void updateReply_Success() {
            // given
            ReviewReplyRequestDto requestDto = new ReviewReplyRequestDto("수정된 답글입니다!");

            given(reviewReplyRepository.findByReviewIdAndNotDeleted(reviewId)).willReturn(Optional.of(testReply));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(testStore));
            given(userRepository.findById(owner.getId())).willReturn(Optional.of(owner));

            // when
            ReviewReplyResponseDto result = reviewReplyService.updateReply(reviewId, owner.getId(), requestDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo("수정된 답글입니다!");

            verify(reviewReplyRepository).findByReviewIdAndNotDeleted(reviewId);
            verify(storeRepository).findByStoreIdAndNotDeleted(storeId);
            verify(userRepository).findById(owner.getId());
        }

        @Test
        @DisplayName("답글 수정 실패 - 존재하지 않는 답글")
        void updateReply_Fail_ReplyNotFound() {
            // given
            ReviewReplyRequestDto requestDto = new ReviewReplyRequestDto("수정된 답글입니다!");

            given(reviewReplyRepository.findByReviewIdAndNotDeleted(reviewId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewReplyService.updateReply(reviewId, owner.getId(), requestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 답글입니다.");
        }

        @Test
        @DisplayName("답글 수정 실패 - 가게 소유자가 아님")
        void updateReply_Fail_NotStoreOwner() {
            // given
            ReviewReplyRequestDto requestDto = new ReviewReplyRequestDto("수정된 답글입니다!");
            User otherUser = User.builder()
                    .username("other")
                    .password("password123")
                    .nickname("다른사람")
                    .authority(UserAuthority.OWNER)
                    .build();

            given(reviewReplyRepository.findByReviewIdAndNotDeleted(reviewId)).willReturn(Optional.of(testReply));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(testStore));
            given(userRepository.findById(3L)).willReturn(Optional.of(otherUser));

            // when & then
            assertThatThrownBy(() -> reviewReplyService.updateReply(reviewId, 3L, requestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("해당 가게의 소유자만 답글을 작성할 수 있습니다.");
        }
    }

    @Nested
    @DisplayName("답글 삭제 테스트")
    class DeleteReplyTest {

        @Test
        @DisplayName("답글 삭제 성공")
        void deleteReply_Success() {
            // given
            given(reviewReplyRepository.findByReviewIdAndNotDeleted(reviewId)).willReturn(Optional.of(testReply));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(testStore));
            given(userRepository.findById(owner.getId())).willReturn(Optional.of(owner));

            // when
            reviewReplyService.deleteReply(reviewId, owner.getId());

            // then
            verify(reviewReplyRepository).findByReviewIdAndNotDeleted(reviewId);
            verify(storeRepository).findByStoreIdAndNotDeleted(storeId);
            verify(userRepository).findById(owner.getId());
        }

        @Test
        @DisplayName("답글 삭제 실패 - 존재하지 않는 답글")
        void deleteReply_Fail_ReplyNotFound() {
            // given
            given(reviewReplyRepository.findByReviewIdAndNotDeleted(reviewId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewReplyService.deleteReply(reviewId, owner.getId()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 답글입니다.");
        }

        @Test
        @DisplayName("답글 삭제 실패 - 가게 소유자가 아님")
        void deleteReply_Fail_NotStoreOwner() {
            // given
            User otherUser = User.builder()
                    .username("other")
                    .password("password123")
                    .nickname("다른사람")
                    .authority(UserAuthority.OWNER)
                    .build();

            given(reviewReplyRepository.findByReviewIdAndNotDeleted(reviewId)).willReturn(Optional.of(testReply));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(testStore));
            given(userRepository.findById(3L)).willReturn(Optional.of(otherUser));

            // when & then
            assertThatThrownBy(() -> reviewReplyService.deleteReply(reviewId, 3L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("해당 가게의 소유자만 답글을 작성할 수 있습니다.");
        }
    }

    @Nested
    @DisplayName("가게 소유자 검증 테스트")
    class ValidateStoreOwnerTest {

        @Test
        @DisplayName("MANAGER 권한으로 답글 등록 가능")
        void createReply_WithManagerAuthority() {
            // given
            User manager = User.builder()
                    .username("manager")
                    .password("password123")
                    .nickname("관리자")
                    .authority(UserAuthority.MANAGER)
                    .build();

            ReviewReplyRequestDto requestDto = new ReviewReplyRequestDto("관리자 답글입니다!");

            given(reviewRepository.findByIdAndNotDeleted(reviewId)).willReturn(Optional.of(testReview));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(testStore));
            given(userRepository.findById(manager.getId())).willReturn(Optional.of(manager));
            given(reviewReplyRepository.findByReviewIdAndNotDeleted(reviewId)).willReturn(Optional.empty());
            given(reviewReplyRepository.save(any(ReviewReply.class))).willReturn(testReply);

            // when
            ReviewReplyResponseDto result = reviewReplyService.createReply(reviewId, manager.getId(), requestDto);

            // then
            assertThat(result).isNotNull();
            verify(reviewReplyRepository).save(any(ReviewReply.class));
        }

        @Test
        @DisplayName("MASTER 권한으로 답글 등록 가능")
        void createReply_WithMasterAuthority() {
            // given
            User master = User.builder()
                    .username("master")
                    .password("password123")
                    .nickname("최종관리자")
                    .authority(UserAuthority.MASTER)
                    .build();

            ReviewReplyRequestDto requestDto = new ReviewReplyRequestDto("마스터 답글입니다!");

            given(reviewRepository.findByIdAndNotDeleted(reviewId)).willReturn(Optional.of(testReview));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(testStore));
            given(userRepository.findById(master.getId())).willReturn(Optional.of(master));
            given(reviewReplyRepository.findByReviewIdAndNotDeleted(reviewId)).willReturn(Optional.empty());
            given(reviewReplyRepository.save(any(ReviewReply.class))).willReturn(testReply);

            // when
            ReviewReplyResponseDto result = reviewReplyService.createReply(reviewId, master.getId(), requestDto);

            // then
            assertThat(result).isNotNull();
            verify(reviewReplyRepository).save(any(ReviewReply.class));
        }

        @Test
        @DisplayName("CUSTOMER 권한으로 답글 등록 불가")
        void createReply_Fail_WithCustomerAuthority() {
            // given
            ReviewReplyRequestDto requestDto = new ReviewReplyRequestDto("고객 답글입니다!");

            given(reviewRepository.findByIdAndNotDeleted(reviewId)).willReturn(Optional.of(testReview));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(testStore));
            given(userRepository.findById(customer.getId())).willReturn(Optional.of(customer));

            // when & then
            assertThatThrownBy(() -> reviewReplyService.createReply(reviewId, customer.getId(), requestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("해당 가게의 소유자만 답글을 작성할 수 있습니다.");

            verify(reviewReplyRepository, never()).save(any());
        }
    }
}