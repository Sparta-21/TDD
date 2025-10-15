package com.sparta.tdd.domain.store.service;

import static com.sparta.tdd.domain.store.entity.QStore.store;

import com.querydsl.core.Tuple;
import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.menu.dto.MenuWithStoreResponseDto;
import com.sparta.tdd.domain.menu.entity.QMenu;
import com.sparta.tdd.domain.store.dto.StoreRequestDto;
import com.sparta.tdd.domain.store.dto.StoreResponseDto;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.domain.store.repository.StoreRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.domain.user.repository.UserRepository;
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    public Page<StoreResponseDto> searchStoresByKeywordAndCategoryWithMenus(String keyword,
        StoreCategory storeCategory,
        Pageable pageable) {

        List<UUID> storeIds = storeRepository.findPagedStoreIdsByKeyword(pageable, keyword,
            storeCategory);

        if (storeIds.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Tuple> tuples = storeRepository.findStoresWithMenusByIds(storeIds);

        Map<UUID, List<MenuWithStoreResponseDto>> menus = tuples.stream()
            .filter(tuple -> Objects.nonNull(tuple.get(QMenu.menu)))
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(store)).getId(),
                Collectors.mapping(
                    tuple -> MenuWithStoreResponseDto.from(
                        Objects.requireNonNull(tuple.get(QMenu.menu))),
                    Collectors.toList()
                )
            ));

        List<StoreResponseDto> stores = tuples.stream()
            .map(tuple -> tuple.get(store))
            .filter(Objects::nonNull)
            .distinct()
            .map(StoreResponseDto::from)
            .map(storeResponseDto -> storeResponseDto.withMenus(
                menus.getOrDefault(storeResponseDto.id(), List.of())))
            .toList();

        long totalCount;
        if (stores.size() < pageable.getPageSize()) {
            totalCount = pageable.getOffset() + stores.size();
        } else {
            totalCount = storeRepository.countStoresByKeyword(keyword, storeCategory);
        }

        return new PageImpl<>(stores, pageable, totalCount);
    }

    @Transactional
    public StoreResponseDto createStore(Long userId, @Valid StoreRequestDto requestDto) {
        User user = getUserById(userId);
        Store store = requestDto.toEntity(user);

        return StoreResponseDto.from(storeRepository.save(store));
    }

    public StoreResponseDto getStore(UUID storeId) {
        return StoreResponseDto.from(getStoreById(storeId));
    }

    @Transactional
    public void updateStore(UserDetailsImpl userDetails, UUID storeId,
        @Valid StoreRequestDto requestDto) {
        User user = getUserById(userDetails.getUserId());
        Store store = getStoreById(storeId);
        validateStoreOwnership(user, store);

        store.updateStore(user, requestDto);
    }

    @Transactional
    public void deleteStore(Long userId, UUID storeId) {
        User user = getUserById(userId);
        Store store = getStoreById(storeId);
        validateStoreOwnership(user, store);

        store.delete(user.getId());
    }

    private Store getStoreById(UUID storeId) {
        return storeRepository.findByStoreIdAndNotDeleted(storeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateStoreOwnership(User user, Store store) {
        if (!store.isOwner(user) && !UserAuthority.isManagerLevel(user.getAuthority())) {
            throw new BusinessException(ErrorCode.STORE_OWNERSHIP_DENIED);
        }
    }
}
