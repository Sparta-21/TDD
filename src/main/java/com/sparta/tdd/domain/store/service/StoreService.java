package com.sparta.tdd.domain.store.service;

import com.sparta.tdd.domain.menu.dto.MenuWithStoreResponseDto;
import com.sparta.tdd.domain.menu.repository.MenuRepository;
import com.sparta.tdd.domain.store.dto.StoreRequestDto;
import com.sparta.tdd.domain.store.dto.StoreResponseDto;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.domain.store.repository.StoreRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;

    // TODO: 정렬 추가 (별점 순)
    public Page<StoreResponseDto> searchStoresByKeywordAndCategoryWithMenus(String keyword,
        StoreCategory storeCategory,
        Pageable pageable) {

        List<UUID> menuMatched = storeRepository.findStoreIdsByMenuKeyword(keyword, storeCategory);
        List<UUID> storeMatched = storeRepository.findStoreIdsByStoreNameKeyword(keyword,
            storeCategory);

        LinkedHashSet<UUID> searchedIds = new LinkedHashSet<>();
        searchedIds.addAll(menuMatched);
        searchedIds.addAll(storeMatched);

        if (searchedIds.isEmpty()) {
            return Page.empty(pageable);
        }

        List<UUID> storeIds = new ArrayList<>(searchedIds);

        Page<StoreResponseDto> stores = storeRepository.findStoresByIds(storeIds, pageable);

        List<UUID> pagedStoreIds = stores.getContent().stream()
            .map(StoreResponseDto::id)
            .toList();

        Map<UUID, List<MenuWithStoreResponseDto>> menuMap = menuRepository.findByStoreIds(
            pagedStoreIds);

        return stores.map(store -> store.withMenus(menuMap.getOrDefault(store.id(), List.of())));
    }

    @Transactional
    public StoreResponseDto createStore(Long userId, @Valid StoreRequestDto requestDto) {
        User user = getUserById(userId);
        validateStorePermission(user);

        Store store = requestDto.toEntity(user);

        Store savedStore = storeRepository.save(store);
        return StoreResponseDto.from(savedStore);
    }

    public StoreResponseDto getStore(UUID storeId) {
        Store store = getStoreById(storeId);
        return StoreResponseDto.from(store);
    }

    @Transactional
    public void updateStore(Long userId, UUID storeId, @Valid StoreRequestDto requestDto) {
        User user = getUserById(userId);
        Store store = getStoreById(storeId);
        validateStoreOwnership(user, store);

        store.updateName(requestDto.name());
        store.updateUser(user);
        store.updateCategory(requestDto.category());
        store.updateDescription(requestDto.description());
        store.updateImageUrl(requestDto.imageUrl());
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
            .orElseThrow(() -> new EntityNotFoundException("상점이 존재하지 않습니다."));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("유저가 존재하지 않습니다."));
    }

    private void validateStorePermission(User user) {
        if (!user.isOwnerLevel()) {
            throw new IllegalArgumentException("상점 관련 작업을 수행할 권한이 없습니다.");
        }
    }

    private void validateStoreOwnership(User user, Store store) {
        if (!store.isOwner(user) && !user.isManagerLevel()) {
            throw new IllegalArgumentException("본인의 상점만 수정/삭제할 수 있습니다.");
        }
    }
}
