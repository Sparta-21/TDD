package com.sparta.tdd.domain.review.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.review.entity.Review;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.global.config.AuditConfig;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuditConfig.class)
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private EntityManager em;

    private User testUser;
    private Store testStore;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .username("testuser")
            .password("password123")
            .nickname("테스트유저")
            .authority(UserAuthority.CUSTOMER)
            .build();
        em.persist(testUser);

        testStore = Store.builder()
            .name("테스트 가게")
            .category(StoreCategory.KOREAN)
            .description("맛있는 가게")
            .user(testUser)
            .build();
        em.persist(testStore);

        testOrder = Order.builder()
            .price(10000)
            .user(testUser)
            .store(testStore)
            .build();
        em.persist(testOrder);

        em.flush();
        em.clear();
    }

    private Review createReview(Integer rating, String imageUrl, String content) {
        User user = em.find(User.class, testUser.getId());
        Store store = em.find(Store.class, testStore.getId());
        Order order = em.find(Order.class, testOrder.getId());

        return new Review(null, rating, imageUrl, content, user, store, order);
    }

    @Test
    @DisplayName("리뷰 저장")
    void 리뷰저장() {
        // given
        Review review = createReview(5, "이미지", "good");

        // when
        Review savedReview = reviewRepository.save(review);

        // then
        assertThat(savedReview.getId()).isNotNull();
        assertThat(savedReview.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(savedReview.getRating()).isEqualTo(5);
        assertThat(savedReview.getContent()).isEqualTo("good");
    }

    @Test
    @DisplayName("ID로 리뷰를 조회할 수 있다")
    void 리뷰조회() {
        // given
        Review review = createReview(5, "이미지", "맛있어요");
        Review savedReview = reviewRepository.save(review);

        // when
        Optional<Review> foundReview = reviewRepository.findById(savedReview.getId());

        // then
        assertThat(foundReview).isPresent();
        assertThat(foundReview.get().getContent()).isEqualTo("맛있어요");
    }

    @Test
    @DisplayName("삭제된지 않은 ID로 리뷰를 조회할 수 있다")
    void 삭제되지않은리뷰조회2() {
        // given
        Review review = createReview(5, "이미지", "맛있어요");
        Review savedReview = reviewRepository.save(review);

        // when
        Optional<Review> foundReview = reviewRepository.findByIdAndNotDeleted(savedReview.getId());

        // then
        assertThat(foundReview).isPresent();
        assertThat(foundReview.get().getContent()).isEqualTo("맛있어요");
    }

    @Test
    @DisplayName("리뷰를 수정할 수 있다")
    void 리뷰수정() {
        // given
        Review review = createReview(3, "이미지", "보통이에요");
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
        Review review = createReview(4, null, "좋아요");
        Review savedReview = reviewRepository.save(review);

        // when
        savedReview.delete(1L);
        reviewRepository.save(savedReview);

        // then
        Review deletedReview = reviewRepository.findById(savedReview.getId()).get();
        assertThat(deletedReview.isDeleted()).isTrue();
        assertThat(deletedReview.getDeletedAt()).isNotNull();
        assertThat(deletedReview.getDeletedBy()).isEqualTo(1L);
    }

    @Test
    @DisplayName("특정 가게의 리뷰 목록을 조회할 수 있다")
    void 리뷰목록조회_가게() {
        // given
        User user = em.find(User.class, testUser.getId());
        Store store1 = em.find(Store.class, testStore.getId());

        Store store2 = Store.builder()
            .name("다른 가게")
            .category(StoreCategory.KOREAN)
            .user(user)
            .build();
        em.persist(store2);

        Order order1 = em.find(Order.class, testOrder.getId());
        Order order2 = Order.builder().price(5000).user(user).store(store1).build();
        Order order3 = Order.builder().price(7000).user(user).store(store2).build();
        em.persist(order2);
        em.persist(order3);

        Review review1 = new Review(null, 5, null, "맛있어요", user, store1, order1);
        Review review2 = new Review(null, 4, null, "좋아요", user, store1, order2);
        Review review3 = new Review(null, 3, null, "다른 가게", user, store2, order3);

        reviewRepository.save(review1);
        reviewRepository.save(review2);
        reviewRepository.save(review3);

        // when
        List<Review> storeReviews = reviewRepository.findByStoreIdAndNotDeleted(store1.getId());

        // then
        assertThat(storeReviews).hasSize(2);
    }

    @Test
    @DisplayName("특정 유저의 리뷰 목록을 조회할 수 있다")
    void 리뷰목록조회_개인() {
        // given
        User user1 = em.find(User.class, testUser.getId());
        User user2 = User.builder()
            .username("user2")
            .password("pass2")
            .nickname("유저2")
            .authority(UserAuthority.CUSTOMER)
            .build();
        em.persist(user2);

        Store store1 = em.find(Store.class, testStore.getId());
        Store store2 = Store.builder()
            .name("가게2")
            .category(StoreCategory.KOREAN)
            .user(user1)
            .build();
        em.persist(store2);

        Order order1 = em.find(Order.class, testOrder.getId());
        Order order2 = Order.builder().price(5000).user(user1).store(store2).build();
        Order order3 = Order.builder().price(7000).user(user2).store(store1).build();
        em.persist(order2);
        em.persist(order3);

        Review review1 = new Review(null, 5, null, "맛있어요", user1, store1, order1);
        Review review2 = new Review(null, 4, null, "좋아요", user1, store2, order2);
        Review review3 = new Review(null, 3, null, "보통이에요", user2, store1, order3);

        reviewRepository.save(review1);
        reviewRepository.save(review2);
        reviewRepository.save(review3);

        // when
        List<Review> userReviews = reviewRepository.findByUserIdAndNotDeleted(user1.getId());

        // then
        assertThat(userReviews).hasSize(2);

    }

    @Test
    @DisplayName("모든 리뷰 확인 매니저 이상 급")
    void 리뷰목록조회_전체() {
        // given
        User user = em.find(User.class, testUser.getId());
        Store store = em.find(Store.class, testStore.getId());

        Order order1 = em.find(Order.class, testOrder.getId());
        Order order2 = Order.builder().price(5000).user(user).store(store).build();
        Order order3 = Order.builder().price(7000).user(user).store(store).build();
        em.persist(order2);
        em.persist(order3);

        Review review1 = new Review(null, 5, null, "맛있어요", user, store, order1);
        Review review2 = new Review(null, 4, null, "좋아요", user, store, order2);
        Review review3 = new Review(null, 3, null, "보통이에요", user, store, order3);

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
        User user = em.find(User.class, testUser.getId());
        Store store = em.find(Store.class, testStore.getId());

        Order order1 = em.find(Order.class, testOrder.getId());
        Order order2 = Order.builder().price(5000).user(user).store(store).build();
        Order order3 = Order.builder().price(7000).user(user).store(store).build();
        em.persist(order2);
        em.persist(order3);

        Review review1 = new Review(null, 5, null, "맛있어요", user, store, order1);
        Review review2 = new Review(null, 4, null, "좋아요", user, store, order2);
        Review review3 = new Review(null, 3, null, "보통이에요", user, store, order3);

        reviewRepository.save(review1);
        reviewRepository.save(review2);
        reviewRepository.save(review3);

        // when
        Double average = reviewRepository.findAverageRatingByStoreId(store.getId());

        // then
        assertThat(average).isEqualTo(4.0);

    }
}