package com.sparta.tdd.domain.menu.controller;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.menu.dto.MenuRequestDto;
import com.sparta.tdd.domain.menu.dto.MenuResponseDto;
import com.sparta.tdd.domain.menu.service.MenuService;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
@RequiredArgsConstructor
@RequestMapping("/v1/store")
public class MenuController {

    private final MenuService menuService;

    @Operation(
        summary = "메뉴 목록 조회",
        description = "가게의 메뉴 목록을 조회합니다. OWERN나 MASTER가 아니면 숨겨진 메뉴를 볼 수 없습니다."
    )
    @GetMapping("/{storeId}/menu")
    public ResponseEntity<List<MenuResponseDto>> getMenus(@PathVariable UUID storeId,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserAuthority authority = userDetails.getUserAuthority();
        return ResponseEntity.status(HttpStatus.OK)
            .body(menuService.getMenus(storeId, authority));
    }

    @Operation(
        summary = "메뉴 상세 조회",
        description = "가게의 특정 메뉴를 조회합니다. OWNER나 MASTER가 아니면 숨겨진 메뉴를 볼 수 없습니다. "
    )
    @GetMapping("/{storeId}/menu/{menuId}")
    public ResponseEntity<MenuResponseDto> getMenu(@PathVariable UUID storeId,
        @PathVariable UUID menuId,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserAuthority authority = userDetails.getUserAuthority();
        return ResponseEntity.status(HttpStatus.OK)
            .body(menuService.getMenu(storeId, menuId, authority));
    }

    @Operation(
        summary = "메뉴 등록",
        description = "가게의 메뉴를 등록합니다. OWNER나 MASTER만 가능하며, 해당 가게 점주가 아니라면 예외가 발생합니다. "
    )
    @PreAuthorize("hasAnyRole('OWNER', 'MASTER')")
    @PostMapping("/{storeId}/menu")
    public ResponseEntity<MenuResponseDto> createMenu(@PathVariable UUID storeId,
        @Valid @RequestBody MenuRequestDto menuRequestDto,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(menuService.createMenu(storeId, menuRequestDto, userId));
    }

    @Operation(
        summary = "메뉴 수정",
        description = "가게의 메뉴를 수정합니다. OWNER나 MASTER만 가능하며, 해당 가게 점주가 아니라면 예외가 발생합니다. "
    )
    @PreAuthorize("hasAnyRole('OWNER', 'MASTER')")
    @PatchMapping("/{storeId}/menu/{menuId}")
    public ResponseEntity<Void> updateMenu(@PathVariable UUID storeId, @PathVariable UUID menuId,
        @RequestBody MenuRequestDto menuRequestDto,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUserId();
        menuService.updateMenu(storeId, menuId, menuRequestDto, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "메뉴 상태 변경",
        description = "가게의 메뉴 상태를 변경합니다. 메뉴를 숨기거나, 공개할 수 있습니다. OWNER나 MASTER만 가능하며, 해당 가게 점주가 아니라면 예외가 발생합니다. "
    )
    @PreAuthorize("hasAnyRole('OWNER', 'MASTER')")
    @PatchMapping("/{storeId}/menu/{menuId}/status")
    public ResponseEntity<Void> updateMenuStatus(@PathVariable UUID storeId,
        @PathVariable UUID menuId,
        @RequestParam Boolean status,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUserId();
        menuService.updateMenuStatus(storeId, menuId, status, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "메뉴 삭제",
        description = "가게의 메뉴를 삭제합니다. OWNER나 MASTER만 가능하며, 해당 가게 점주가 아니라면 예외가 발생합니다. 메뉴는 soft delete 됩니다. "
    )
    @PreAuthorize("hasAnyRole('OWNER', 'MASTER')")
    @DeleteMapping("/{storeId}/menu/{menuId}")
    public ResponseEntity<Void> deleteMenu(@PathVariable UUID storeId, @PathVariable UUID menuId,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUserId();
        menuService.deleteMenu(storeId, menuId, userId);
        return ResponseEntity.noContent().build();
    }


}
