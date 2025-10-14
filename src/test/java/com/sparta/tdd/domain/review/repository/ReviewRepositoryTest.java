package com.sparta.tdd.domain.review.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sparta.tdd.common.template.RepositoryTest;
import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.enums.OrderStatus;
import com.sparta.tdd.domain.order.repository.OrderRepository;
import com.sparta.tdd.domain.review.entity.Review;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.domain.store.repository.StoreRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.domain.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DisplayName("ReviewRepository 테스트")
class ReviewRepositoryTest extends RepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private OrderRepository orderRepository;

    private User testUser;
    private Store testStore;
    private Order testOrder;
    private Review testReview;

    @BeforeEach
    void 필요세팅() {
        // 테스트용 사용자 생성
        testUser = User.builder()
            .username("testuser")
            .password("password123")
            .nickname("테스트유저")
            .authority(UserAuthority.CUSTOMER)
            .build();
        testUser = userRepository.save(testUser);

        // 테스트용 가게 생성
        testStore = Store.builder()
            .name("테스트 가게")
            .category(StoreCategory.KOREAN)
            .description("맛있는 한식당")
            .user(testUser)
            .build();
        testStore = storeRepository.save(testStore);

        // 테스트용 주문 생성
        testOrder = Order.builder()
            .address("서울시 강남구")
            .orderStatus(OrderStatus.DELIVERED)
            .store(testStore)
            .user(testUser)
            .build();
        testOrder = orderRepository.save(testOrder);

        // 테스트용 리뷰 생성
        testReview = Review.builder()
            .user(testUser)
            .store(testStore)
            .order(testOrder)
            .rating(5)
            .imageUrl("http://example.com/image.jpg")
            .content("정말 맛있어요!")
            .build();
        testReview = reviewRepository.save(testReview);
    }

    @Test
    @DisplayName("삭제되지 않은 리뷰 조회 - 성공")
    void 삭제되지_않은_리뷰조회_성공해야함() {
        //given
        // when
        Optional<Review> result = reviewRepository.findByIdAndNotDeleted(testReview.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testReview.getId());
        assertThat(result.get().getContent()).isEqualTo("정말 맛있어요!");
    }

    @Test
    @DisplayName("삭제된 리뷰 조회 - 조회 안됨")
    void 삭제되지_않은_리뷰조회_실패() {
        // given
        testReview.delete(testUser.getId());
        reviewRepository.save(testReview);

        // when
        Optional<Review> result = reviewRepository.findByIdAndNotDeleted(testReview.getId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("특정 유저의 삭제되지 않은 리뷰 목록 조회")
    void 리뷰조회() {
        // given
        Order order2 = Order.builder()
            .address("서울시 서초구")
            .orderStatus(OrderStatus.DELIVERED)
            .store(testStore)
            .user(testUser)
            .build();
        order2 = orderRepository.save(order2);

        Review review2 = Review.builder()
            .user(testUser)
            .store(testStore)
            .order(order2)
            .rating(4)
            .content("두 번째 리뷰")
            .build();
        reviewRepository.save(review2);

        // when
        List<Review> result = reviewRepository.findByUserIdAndNotDeleted(testUser.getId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Review::getUserId)
            .containsOnly(testUser.getId());
    }

    @Test
    @DisplayName("특정 가게의 삭제되지 않은 리뷰 목록 조회")
    void 리뷰목록조회() {
        // given
        Order order2 = Order.builder()
            .address("서울시 송파구")
            .orderStatus(OrderStatus.DELIVERED)
            .store(testStore)
            .user(testUser)
            .build();
        order2 = orderRepository.save(order2);

        Review review2 = Review.builder()
            .user(testUser)
            .store(testStore)
            .order(order2)
            .rating(3)
            .content("보통이에요")
            .build();
        reviewRepository.save(review2);

        // when
        List<Review> result = reviewRepository.findByStoreIdAndNotDeleted(testStore.getId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Review::getStoreId)
            .containsOnly(testStore.getId());
    }

    @Test
    @DisplayName("특정 가게의 리뷰 페이징 조회")
    void 리뷰페이징() {
        // given
        for (int i = 0; i < 15; i++) {
            Order order = Order.builder()
                .address("서울시 관악구 " + i)
                .orderStatus(OrderStatus.DELIVERED)
                .store(testStore)
                .user(testUser)
                .build();
            order = orderRepository.save(order);

            Review review = Review.builder()
                .user(testUser)
                .store(testStore)
                .order(order)
                .rating(4)
                .content("리뷰 " + i)
                .build();
            reviewRepository.save(review);
        }

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Review> result = reviewRepository.findPageByStoreIdAndNotDeleted(testStore.getId(), pageable);

        // then
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getTotalElements()).isEqualTo(16); // 기존 1개 + 추가 15개
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("특정 가게의 평균 평점 조회")
    void 평균조회() {
        // given
        Order order2 = Order.builder()
            .address("서울시 마포구")
            .orderStatus(OrderStatus.DELIVERED)
            .store(testStore)
            .user(testUser)
            .build();
        order2 = orderRepository.save(order2);

        Review review2 = Review.builder()
            .user(testUser)
            .store(testStore)
            .order(order2)
            .rating(3)
            .content("보통이에요")
            .build();
        reviewRepository.save(review2);

        // when
        Double avgRating = reviewRepository.findAverageRatingByStoreId(testStore.getId());

        // then
        assertThat(avgRating).isEqualTo(4.0); // (5 + 3) / 2 = 4.0
    }

    @Test
    @DisplayName("리뷰가 없는 가게의 평균 평점 조회")
    void 빵점조회() {
        // given
        Store newStore = Store.builder()
            .name("새로운 가게")
            .category(StoreCategory.CHINESE)
            .user(testUser)
            .build();
        newStore = storeRepository.save(newStore);

        // when
        Double avgRating = reviewRepository.findAverageRatingByStoreId(newStore.getId());

        // then
        assertThat(avgRating).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 리뷰 조회")
    void 없는리뷰조회() {
        // given
        UUID nonExistentId = UUID.randomUUID();

        // when
        Optional<Review> result = reviewRepository.findByIdAndNotDeleted(nonExistentId);

        // then
        assertThat(result).isEmpty();
    }
}