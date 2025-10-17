package com.sparta.tdd.domain.store.controller;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.store.dto.StoreRequestDto;
import com.sparta.tdd.domain.store.dto.StoreResponseDto;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.domain.store.service.StoreService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

//    @GetMapping
//    public ResponseEntity<Page<StoreResponseDto>> searchStores(
//        @RequestParam(required = false) String keyword,
//        @RequestParam(required = false) StoreCategory storeCategory,
//        Pageable pageable) {
//        Page<StoreResponseDto> responseDto = storeService.searchStoresByKeywordAndCategoryWithMenus(
//            keyword, storeCategory, pageable);
//        return ResponseEntity.ok(responseDto);
//    }

    @GetMapping
    public ResponseEntity<Page<StoreResponseDto>> searchStores2(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) StoreCategory storeCategory,
        Pageable pageable) {
        Page<StoreResponseDto> responseDto = storeService.searchStoresByKeywordAndCategoryWithMenus(
            keyword, storeCategory, pageable);
        return ResponseEntity.ok(responseDto);
    }

//    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
//    @PostMapping
//    public ResponseEntity<StoreResponseDto> createStore(
//        @Valid @RequestBody StoreRequestDto requestDto,
//        @AuthenticationPrincipal UserDetailsImpl user) {
//
//        StoreResponseDto responseDto = storeService.createStore(user.getUserId(), requestDto);
//
//        URI location = URI.create("/v1/stores/" + responseDto.id());
//
//        return ResponseEntity
//            .created(location)
//            .body(responseDto);
//    }

//    @GetMapping("{storeId}")
//    public ResponseEntity<StoreResponseDto> getStore(@PathVariable UUID storeId) {
//        StoreResponseDto responseDto = storeService.getStore(storeId);
//        return ResponseEntity.ok(responseDto);
//    }

    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @PatchMapping("{storeId}")
    public ResponseEntity<Void> updateStore(
        @PathVariable UUID storeId,
        @Valid @RequestBody StoreRequestDto requestDto,
        @AuthenticationPrincipal UserDetailsImpl user) {

        storeService.updateStore(user, storeId, requestDto);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @DeleteMapping("{storeId}")
    public ResponseEntity<Void> deleteStore(
        @PathVariable UUID storeId,
        @AuthenticationPrincipal UserDetailsImpl user) {

        storeService.deleteStore(user.getUserId(), storeId);
        return ResponseEntity.noContent().build();
    }
}
