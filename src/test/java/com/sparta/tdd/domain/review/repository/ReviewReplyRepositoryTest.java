package com.sparta.tdd.domain.review.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sparta.tdd.common.template.RepositoryTest;
import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.enums.OrderStatus;
import com.sparta.tdd.domain.order.repository.OrderRepository;
import com.sparta.tdd.domain.review.entity.Review;
import com.sparta.tdd.domain.review.entity.ReviewReply;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.domain.store.repository.StoreRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.domain.user.repository.UserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("ReviewReplyRepository 테스트")
class ReviewReplyRepositoryTest extends RepositoryTest {

    @Autowired
    private ReviewReplyRepository reviewReplyRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private OrderRepository orderRepository;

    private User customer;
    private User owner;
    private Store testStore;
    private Order testOrder;
    private Review testReview;
    private ReviewReply testReply;

    @BeforeEach
    protected void setUp() {
        // 고객 사용자 생성
        customer = User.builder()
            .username("customer")
            .password("password123")
            .nickname("고객")
            .authority(UserAuthority.CUSTOMER)
            .build();
        customer = userRepository.save(customer);

        // 가게 사장 사용자 생성
        owner = User.builder()
            .username("owner")
            .password("password123")
            .nickname("사장님")
            .authority(UserAuthority.OWNER)
            .build();
        owner = userRepository.save(owner);

        // 테스트용 가게 생성
        testStore = Store.builder()
            .name("테스트 가게")
            .category(StoreCategory.KOREAN)
            .description("맛있는 한식당")
            .user(owner)
            .build();
        testStore = storeRepository.save(testStore);

        // 테스트용 주문 생성
        testOrder = Order.builder()
            .address("서울시 강남구")
            .orderStatus(OrderStatus.DELIVERED)
            .store(testStore)
            .user(customer)
            .build();
        testOrder = orderRepository.save(testOrder);

        // 테스트용 리뷰 생성
        testReview = Review.builder()
            .user(customer)
            .store(testStore)
            .order(testOrder)
            .rating(5)
            .imageUrl("http://example.com/image.jpg")
            .content("정말 맛있어요!")
            .build();
        testReview = reviewRepository.save(testReview);

        // 테스트용 답글 생성
        testReply = ReviewReply.builder()
            .review(testReview)
            .content("감사합니다!")
            .ownerId(owner.getId())
            .build();
        testReply = reviewReplyRepository.save(testReply);
    }

    @Test
    @DisplayName("특정 리뷰의 삭제되지 않은 답글 조회 - 성공")
    void 답글조회() {
        // when
        Optional<ReviewReply> result = reviewReplyRepository.findByReviewIdAndNotDeleted(testReview.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testReply.getId());
        assertThat(result.get().getContent()).isEqualTo("감사합니다!");
        assertThat(result.get().getOwnerId()).isEqualTo(owner.getId());
    }

    @Test
    @DisplayName("삭제된 답글 조회 - 조회 안됨")
    void 삭제답글조회() {
        // given
        testReply.delete(owner.getId());
        reviewReplyRepository.save(testReply);

        // when
        Optional<ReviewReply> result = reviewReplyRepository.findByReviewIdAndNotDeleted(testReview.getId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("답글이 없는 리뷰 조회")
    void 노답글조회() {
        // given
        Order newOrder = Order.builder()
            .address("서울시 서초구")
            .orderStatus(OrderStatus.DELIVERED)
            .store(testStore)
            .user(customer)
            .build();
        newOrder = orderRepository.save(newOrder);

        Review newReview = Review.builder()
            .user(customer)
            .store(testStore)
            .order(newOrder)
            .rating(4)
            .content("두 번째 리뷰")
            .build();
        newReview = reviewRepository.save(newReview);

        // when
        Optional<ReviewReply> result = reviewReplyRepository.findByReviewIdAndNotDeleted(newReview.getId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("여러 리뷰의 삭제되지 않은 답글 목록 조회")
    void 리뷰목록조회() {
        // given
        Order order2 = Order.builder()
            .address("서울시 송파구")
            .orderStatus(OrderStatus.DELIVERED)
            .store(testStore)
            .user(customer)
            .build();
        order2 = orderRepository.save(order2);

        Review review2 = Review.builder()
            .user(customer)
            .store(testStore)
            .order(order2)
            .rating(4)
            .content("두 번째 리뷰")
            .build();
        review2 = reviewRepository.save(review2);

        ReviewReply reply2 = ReviewReply.builder()
            .review(review2)
            .content("감사합니다! 또 방문해주세요!")
            .ownerId(owner.getId())
            .build();
        reply2 = reviewReplyRepository.save(reply2);

        Order order3 = Order.builder()
            .address("서울시 관악구")
            .orderStatus(OrderStatus.DELIVERED)
            .store(testStore)
            .user(customer)
            .build();
        order3 = orderRepository.save(order3);

        Review review3 = Review.builder()
            .user(customer)
            .store(testStore)
            .order(order3)
            .rating(3)
            .content("세 번째 리뷰")
            .build();
        review3 = reviewRepository.save(review3);

        ReviewReply reply3 = ReviewReply.builder()
            .review(review3)
            .content("방문 감사합니다")
            .ownerId(owner.getId())
            .build();
        reply3 = reviewReplyRepository.save(reply3);

        List<UUID> reviewIds = Arrays.asList(
            testReview.getId(),
            review2.getId(),
            review3.getId()
        );

        // when
        List<ReviewReply> result = reviewReplyRepository.findByReviewIdsAndNotDeleted(reviewIds);

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(ReviewReply::getReviewId)
            .containsExactlyInAnyOrder(testReview.getId(), review2.getId(), review3.getId());
    }


    @Test
    @DisplayName("존재하지 않는 리뷰 ID로 답글 조회")
    void 잘못된리뷰로조회() {
        // given
        UUID nonExistentId = UUID.randomUUID();

        // when
        Optional<ReviewReply> result = reviewReplyRepository.findByReviewIdAndNotDeleted(nonExistentId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("답글 저장 및 리뷰 연관관계 확인")
    void 진짜제대로된건가() {
        // given
        Order newOrder = Order.builder()
            .address("서울시 마포구")
            .orderStatus(OrderStatus.DELIVERED)
            .store(testStore)
            .user(customer)
            .build();
        newOrder = orderRepository.save(newOrder);

        Review newReview = Review.builder()
            .user(customer)
            .store(testStore)
            .order(newOrder)
            .rating(5)
            .content("새로운 리뷰")
            .build();
        newReview = reviewRepository.save(newReview);

        ReviewReply newReply = ReviewReply.builder()
            .review(newReview)
            .content("감사합니다!")
            .ownerId(owner.getId())
            .build();

        // when
        ReviewReply savedReply = reviewReplyRepository.save(newReply);

        // then
        assertThat(savedReply.getId()).isNotNull();
        assertThat(savedReply.getReview().getId()).isEqualTo(newReview.getId());
        assertThat(savedReply.getReviewId()).isEqualTo(newReview.getId());
        assertThat(savedReply.getOwnerId()).isEqualTo(owner.getId());
    }
}