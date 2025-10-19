package com.sparta.tdd.domain.store.controller;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.store.dto.StoreRequestDto;
import com.sparta.tdd.domain.store.dto.StoreResponseDto;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.domain.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.net.URI;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @Operation(
        summary = "가게 검색",
        description = "키워드, 카테고리, 페이징 정보로 가게를 검색합니다. 가게의 메뉴 정보도 함께 조회됩니다."
    )
    @GetMapping
    public ResponseEntity<Page<StoreResponseDto>> searchStores(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) StoreCategory storeCategory,
        Pageable pageable) {
        Page<StoreResponseDto> responseDto = storeService.searchStoresByKeywordAndCategoryWithMenus(
            keyword, storeCategory, pageable);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
        summary = "가게 생성",
        description = "새로운 가게를 등록합니다. OWNER, MANAGER, MASTER 권한이 필요합니다."
    )
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @PostMapping
    public ResponseEntity<StoreResponseDto> createStore(
        @Valid @RequestBody StoreRequestDto requestDto,
        @AuthenticationPrincipal UserDetailsImpl user) {

        StoreResponseDto responseDto = storeService.createStore(user.getUserId(), requestDto);

        URI location = URI.create("/v1/stores/" + responseDto.id());

        return ResponseEntity
            .created(location)
            .body(responseDto);
    }


    @Operation(
        summary = "가게 조회",
        description = """
            스토어 ID를 통해 가게를 조회 합니다.
            """
    )
    @GetMapping("{storeId}")
    public ResponseEntity<StoreResponseDto> getStore(@PathVariable UUID storeId) {
        StoreResponseDto responseDto = storeService.getStore(storeId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
        summary = "가게 상세 조회",
        description = "스토어 ID를 통해 가게의 상세 정보를 조회합니다."
    )
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @PatchMapping("{storeId}")
    public ResponseEntity<Void> updateStore(
        @PathVariable UUID storeId,
        @Valid @RequestBody StoreRequestDto requestDto,
        @AuthenticationPrincipal UserDetailsImpl user) {

        storeService.updateStore(user, storeId, requestDto);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "가게 정보 수정",
        description = "가게 정보를 수정합니다. OWNER, MANAGER, MASTER 권한이 필요합니다."
    )
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @DeleteMapping("{storeId}")
    public ResponseEntity<Void> deleteStore(
        @PathVariable UUID storeId,
        @AuthenticationPrincipal UserDetailsImpl user) {

        storeService.deleteStore(user.getUserId(), storeId);
        return ResponseEntity.noContent().build();
    }
}
