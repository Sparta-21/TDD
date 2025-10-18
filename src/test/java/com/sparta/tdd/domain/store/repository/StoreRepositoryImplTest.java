package com.sparta.tdd.domain.store.repository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.sparta.tdd.common.template.RepositoryTest;
import com.sparta.tdd.domain.menu.dto.MenuRequestDto;
import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.enums.OrderStatus;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;

@DisplayName("StoreRepositoryImpl 커스텀 쿼리 테스트")
class StoreRepositoryImplTest extends RepositoryTest {

    @Autowired
    private StoreRepository storeRepository;

    private User testUser;
    private Store store1;
    private Store store2;
    private String keyword;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .username("testuser")
            .password("password123")
            .nickname("테스트유저")
            .authority(UserAuthority.CUSTOMER)
            .build();

        em.persist(testUser);

        store1 = createStore("김밥천국", StoreCategory.KOREAN, "image", testUser);
        store2 = createStore("TDD 분식", StoreCategory.KOREAN, "image", testUser);

        em.persist(store1);
        em.persist(store2);

        Menu menu1 = createMenu("김밥", "김밥입니다", 3000, "image", store1);
        Menu menu2 = createMenu("떡볶이", "떡볶이입니다", 5000, "image", store2);
        Menu menu3 = createMenu("김밥", "김밥입니다", 5000, "image", store2);

        em.persist(menu1);
        em.persist(menu2);
        em.persist(menu3);

        // 10개의 가게 생성
        for (int i = 1; i <= 10; i++) {
            Store store = createStore(i + "반점", StoreCategory.CHINESE, "중식 전문점", testUser);
            em.persist(store);

            // 각 가게에 메뉴 3개씩 추가
            for (int j = 1; j <= 3; j++) {
                Menu menu = createMenu("짜장면" + j, "짜장면", 10000, "image", store);
                em.persist(menu);
            }
        }

        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("키워드 및 카테고리 검색 테스트")
    class SearchTest {

        @Test
        @DisplayName("키워드 없음, 카테고리 없음 - 전체 조회")
        void searchWithoutKeywordAndCategory() {
            // given
            pageable = PageRequest.of(0, 10);
            keyword = "";
            StoreCategory category = null;

            //when
            List<UUID> stores = storeRepository.findPagedStoreIdsByKeyword(pageable, keyword,
                category);

            //then
            assertThat(stores).hasSize(10);
        }

        @Test
        @DisplayName("키워드로 검색 - 가게명 또는 메뉴명 포함")
        void searchWithKeyword() {
            // given
            pageable = PageRequest.of(0, 10);
            keyword = "김밥";
            StoreCategory category = null;

            //when
            List<UUID> stores = storeRepository.findPagedStoreIdsByKeyword(pageable, keyword,
                category);

            //then
            assertThat(stores).hasSize(2);
        }

        @Test
        @DisplayName("카테고리로 검색")
        void searchWithCategory() {
            // given
            pageable = PageRequest.of(0, 10);
            keyword = "";
            StoreCategory category = StoreCategory.KOREAN;

            //when
            List<UUID> stores = storeRepository.findPagedStoreIdsByKeyword(pageable, keyword,
                category);

            //then
            assertThat(stores).hasSize(2);
        }

        @Test
        @DisplayName("키워드와 카테고리로 검색")
        void searchWithKeywordAndCategory() {
            // given
            pageable = PageRequest.of(0, 10);
            String keyword = "김밥";
            StoreCategory category = StoreCategory.KOREAN;

            //when
            List<UUID> stores = storeRepository.findPagedStoreIdsByKeyword(pageable, keyword,
                category);

            //then
            assertThat(stores).hasSize(2);
        }

        @Test
        @DisplayName("키워드로 메뉴 이름 검색")
        void searchWithMenuKeyword() {
            // given
            pageable = PageRequest.of(0, 10);
            String keyword = "김밥";

            //when
            List<UUID> stores = storeRepository.findPagedStoreIdsByKeyword(pageable, keyword,
                null);

            //then
            assertThat(stores).hasSize(2);
        }
    }

    @Nested
    @DisplayName("정렬 테스트")
    class SortingTest {

        @Test
        @DisplayName("평균 평점 내림차순 정렬")
        void sortByAvgRatingDesc() throws Exception {
            // given
            pageable = PageRequest.of(0, 10, Direction.DESC, "avgRating");
            setStoreAvgRating(store1, BigDecimal.valueOf(5.0));
            setStoreAvgRating(store2, BigDecimal.valueOf(3.0));

            em.merge(store1);
            em.merge(store2);
            em.flush();
            em.clear();

            //when
            List<UUID> stores = storeRepository.findPagedStoreIdsByKeyword(pageable, "", null);
            List<Store> sortedStores = stores.stream()
                .map(id -> em.find(Store.class, id))
                .toList();

            //then
            assertThat(sortedStores).hasSize(10);
            assertThat(sortedStores.get(0).getAvgRating()).isEqualTo(new BigDecimal("5.0"));
            assertThat(sortedStores.get(1).getAvgRating()).isEqualTo(new BigDecimal("3.0"));
        }

        @Test
        @DisplayName("리뷰 개수 내림차순 정렬")
        void sortByReviewCountDesc() throws Exception {
            // given
            pageable = PageRequest.of(0, 10, Direction.DESC, "reviewCount");
            setReviewCount(store1, 4);
            setReviewCount(store2, 21);

            em.merge(store1);
            em.merge(store2);
            em.flush();
            em.clear();

            //when
            List<UUID> stores = storeRepository.findPagedStoreIdsByKeyword(pageable, "", null);
            List<Store> sortedStores = stores.stream()
                .map(id -> em.find(Store.class, id))
                .toList();

            //then
            assertThat(sortedStores).hasSize(10);
            assertThat(sortedStores.get(0).getReviewCount()).isEqualTo(21);
            assertThat(sortedStores.get(1).getReviewCount()).isEqualTo(4);
        }

        @Test
        @DisplayName("주문 개수 내림차순 정렬")
        void sortByOrderCountDesc() {
            // given
            pageable = PageRequest.of(0, 10, Direction.DESC, "orderCount");
            Order order1 = createOrder(store1, testUser);
            Order order2 = createOrder(store2, testUser);
            Order order3 = createOrder(store2, testUser);

            em.persist(order1);
            em.persist(order2);
            em.persist(order3);

            em.flush();
            em.clear();

            //when
            List<UUID> stores = storeRepository.findPagedStoreIdsByKeyword(pageable, "", null);
            List<Store> sortedStores = stores.stream()
                .map(id -> em.find(Store.class, id))
                .toList();

            //then
            assertThat(sortedStores).hasSize(10);
            assertThat(sortedStores.get(0).getName()).isEqualTo("TDD 분식");
            assertThat(sortedStores.get(1).getName()).isEqualTo("김밥천국");
        }
    }

    @Nested
    @DisplayName("페이징 테스트")
    class PagingTest {

        @Test
        @DisplayName("첫 번째 페이지 조회")
        void getFirstPage() {
            //given
            Pageable pageable = PageRequest.of(0, 5);

            //when
            List<UUID> storeIds = storeRepository.findPagedStoreIdsByKeyword(
                pageable, "", null);

            //then
            assertThat(storeIds).hasSize(5);
        }

        @Test
        @DisplayName("두 번째 페이지 조회")
        void getSecondPage() {
            // given
            Pageable pageable = PageRequest.of(1, 5);

            // when
            List<UUID> storeIds = storeRepository.findPagedStoreIdsByKeyword(
                pageable, "", null);

            // then
            assertThat(storeIds).hasSize(5);
        }
    }


    private Store createStore(String storeName, StoreCategory storeCategory, String description,
        User user) {
        return Store.builder()
            .name(storeName)
            .category(storeCategory)
            .description(description)
            .user(user)
            .build();
    }

    private Menu createMenu(String menuName, String description, int price, String image,
        Store store) {
        return Menu.builder()
            .name(menuName)
            .description(description)
            .price(price)
            .imageUrl(image)
            .store(store)
            .build();
    }

    private Order createOrder(Store store, User user) {
        return Order.builder()
            .address("주소")
            .orderStatus(OrderStatus.DELIVERED)
            .store(store)
            .user(user)
            .build();
    }

    private void setStoreAvgRating(Store store, BigDecimal avgRating) throws Exception {
        Field field = Store.class.getDeclaredField("avgRating");
        field.setAccessible(true);
        field.set(store, avgRating);
    }

    private void setReviewCount(Store store, Integer reviewCount) throws Exception {
        Field field = Store.class.getDeclaredField("reviewCount");
        field.setAccessible(true);
        field.set(store, reviewCount);
    }
}