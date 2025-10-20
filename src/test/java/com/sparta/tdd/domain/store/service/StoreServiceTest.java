package com.sparta.tdd.domain.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.querydsl.core.Tuple;
import com.sparta.tdd.domain.menu.dto.MenuRequestDto;
import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.menu.entity.QMenu;
import com.sparta.tdd.domain.store.dto.StoreResponseDto;
import com.sparta.tdd.domain.store.entity.QStore;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.domain.store.repository.StoreRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.global.config.QueryDSLConfig;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreService 테스트")
@Import(QueryDSLConfig.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private StoreService storeService;

    private User testUser;
    private Store koreanStore;
    private Store chineseStore;
    private Menu koreaMenu;
    private Menu chinaMenu;

    @BeforeEach
    void setUp() throws Exception {
        testUser = createUser("testUser", "password123", "테스트유저", UserAuthority.OWNER);

        koreanStore = createStore("김밥천국", StoreCategory.KOREAN, "테스트용", testUser);
        chineseStore = createStore("홍콩반점", StoreCategory.CHINESE, "테스트용", testUser);

        setStoreId(koreanStore, UUID.randomUUID());
        setStoreId(chineseStore, UUID.randomUUID());
        ;

        koreaMenu = createMenu("김밥", "맛있는 김밥", 3000, koreanStore);
        chinaMenu = createMenu("짜장면", "맛있는 짜장면", 6000, chineseStore);
    }

    @Nested
    @DisplayName("가게 검색 테스트")
    class SearchStoreTest {

        @Test
        @DisplayName("키워드 없이 전체 가게 조회 성공")
        void searchStoresWithoutKeyword() {
            //given
            Pageable pageable = PageRequest.of(0, 10);
            List<UUID> storeIds = List.of(koreanStore.getId(), chineseStore.getId());

            List<Tuple> tuples = createTuples(List.of(koreanStore, chineseStore));

            given(storeRepository.findPagedStoreIdsByKeyword(pageable, "", null))
                .willReturn(storeIds);
            given(storeRepository.findStoresWithMenusByIds(storeIds))
                .willReturn(tuples);

            //when
            Page<StoreResponseDto> result = storeService.searchStoresByKeywordAndCategoryWithMenus(
                "", null, pageable);

            //then
            assertThat(result.getContent()).hasSize(2);
            verify(storeRepository).findPagedStoreIdsByKeyword(pageable, "", null);
        }

        @Test
        @DisplayName("가게명으로 검색 성공")
        void searchStoresWithKeyword() {
            //given
            Pageable pageable = PageRequest.of(0, 10);
            String keyword = "김밥";
            List<UUID> storeIds = List.of(koreanStore.getId());
            List<Tuple> tuples = createTuples(List.of(koreanStore));

            given(storeRepository.findPagedStoreIdsByKeyword(pageable, keyword, null))
                .willReturn(storeIds);
            given(storeRepository.findStoresWithMenusByIds(storeIds))
                .willReturn(tuples);

            //when
            Page<StoreResponseDto> result = storeService.searchStoresByKeywordAndCategoryWithMenus(
                keyword, null, pageable);

            //then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).name()).isEqualTo("김밥천국");
            verify(storeRepository).findPagedStoreIdsByKeyword(pageable, keyword, null);
        }

        @Test
        @DisplayName("겁색 결과 없음")
        void searchStoresWithNoMatch() {
            //given
            Pageable pageable = PageRequest.of(0, 10);
            String keyword = "없습니다";

            given(storeRepository.findPagedStoreIdsByKeyword(pageable, keyword, null))
                .willReturn(List.of());

            //when
            Page<StoreResponseDto> result = storeService.searchStoresByKeywordAndCategoryWithMenus(
                keyword, null, pageable);

            //then
            assertThat(result).isEmpty();
            verify(storeRepository).findPagedStoreIdsByKeyword(pageable, keyword, null);
            verify(storeRepository, never()).findStoresWithMenusByIds(any());
        }
    }

    @Nested
    @DisplayName("정렬 테스트")
    class SortingTest {

        @Test
        @DisplayName("평균 평점 내림차순 정렬")
        void sortByAvgRatingDesc() throws Exception {
            //given
            Pageable pageable = PageRequest.of(0, 10, Direction.DESC, "avgRating");
            List<UUID> storeIds = List.of(koreanStore.getId(), chineseStore.getId());
            List<Tuple> tuples = createTuples(List.of(koreanStore, chineseStore));
            setStoreAvgRating(koreanStore, BigDecimal.valueOf(4.2));
            setStoreAvgRating(chineseStore, BigDecimal.valueOf(4.0));

            given(storeRepository.findPagedStoreIdsByKeyword(pageable, "", null))
                .willReturn(storeIds);
            given(storeRepository.findStoresWithMenusByIds(storeIds))
                .willReturn(tuples);

            //when
            Page<StoreResponseDto> result = storeService.searchStoresByKeywordAndCategoryWithMenus(
                "", null, pageable
            );

            //then
            assertThat(result.getContent().get(0).name()).isEqualTo("김밥천국");
            assertThat(result.getContent().get(1).name()).isEqualTo("홍콩반점");
            assertThat(result.getContent().get(0).avgRating()).isGreaterThan(
                result.getContent().get(1).avgRating());
            verify(storeRepository).findPagedStoreIdsByKeyword(pageable, "", null);
        }
    }

    @Nested
    @DisplayName("페이징 테스트")
    class PagingTest {

        @Test
        @DisplayName("마지막 페이지 - COUNT쿼리 최적화")
        void optimizeCountQueryOnLastPage() {
            //given
            Pageable pageable = PageRequest.of(0, 10);
            List<UUID> storeIds = List.of(koreanStore.getId(), chineseStore.getId());
            List<Tuple> tuples = createTuples(List.of(koreanStore, chineseStore));

            given(storeRepository.findPagedStoreIdsByKeyword(pageable, "", null))
                .willReturn(storeIds);
            given(storeRepository.findStoresWithMenusByIds(storeIds))
                .willReturn(tuples);
            //when
            Page<StoreResponseDto> result = storeService.searchStoresByKeywordAndCategoryWithMenus(
                "", null, pageable);

            //then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            verify(storeRepository, never()).countStoresByKeyword(anyString(), any());
        }
    }

    private User createUser(String username, String password, String nickname,
        UserAuthority userAuthority) {
        return User.builder()
            .username(username)
            .password(password)
            .nickname(nickname)
            .authority(userAuthority)
            .build();
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

    private Menu createMenu(String menuName, String description, int price, Store store) {
        return Menu.builder()
            .name(menuName)
            .description(description)
            .price(price)
            .imageUrl("image")
            .store(store)
            .build();
    }

    private void setStoreId(Store store, UUID storeId) throws Exception {
        Field field = Store.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(store, storeId);
    }

    private void setStoreAvgRating(Store store, BigDecimal avgRating) throws Exception {
        Field field = Store.class.getDeclaredField("avgRating");
        field.setAccessible(true);
        field.set(store, avgRating);
    }

    private List<Tuple> createTuples(List<Store> stores) {
        List<Tuple> tuples = new ArrayList<>();

        for (Store store : stores) {
            Menu menu = store.getCategory() == StoreCategory.KOREAN
                ? koreaMenu
                : chinaMenu;

            Tuple tuple = mock(Tuple.class);
            given(tuple.get(QStore.store)).willReturn(store);
            given(tuple.get(QMenu.menu)).willReturn(menu);

            tuples.add(tuple);
        }
        return tuples;
    }
}