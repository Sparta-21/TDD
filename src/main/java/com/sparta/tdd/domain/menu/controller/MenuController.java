package com.sparta.tdd.domain.menu.controller;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.menu.dto.MenuRequestDto;
import com.sparta.tdd.domain.menu.dto.MenuResponseDto;
import com.sparta.tdd.domain.menu.service.MenuService;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/store")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping("/{storeId}/menu")
    public List<MenuResponseDto> getMenus(@PathVariable UUID storeId,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return menuService.getMenus(storeId);
    }

    @GetMapping("/{storeId}/menu/{menuId}")
    public MenuResponseDto getMenu(@PathVariable UUID storeId, @PathVariable UUID menuId,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return menuService.getMenu(storeId, menuId);
    }

    @PostMapping("/{storeId}/menu")
    public MenuResponseDto createMenu(@PathVariable UUID storeId,
        @RequestBody MenuRequestDto menuRequestDto,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return menuService.createMenu(storeId, menuRequestDto);
    }

    @PatchMapping("/{storeId}/menu/{menuId}")
    public void updateMenu(@PathVariable UUID storeId, @PathVariable UUID menuId,
        @RequestBody MenuRequestDto menuRequestDto,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        menuService.updateMenu(storeId, menuId, menuRequestDto);
    }

    @DeleteMapping("/{storeId}/menu/{menuId}")
    public void deleteMenu(@PathVariable UUID storeId, @PathVariable UUID menuId,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        menuService.deleteMenu(storeId, menuId);
    }


}
