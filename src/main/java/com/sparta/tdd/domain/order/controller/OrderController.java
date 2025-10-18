package com.sparta.tdd.domain.order.controller;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.order.dto.OrderRequestDto;
import com.sparta.tdd.domain.order.dto.OrderResponseDto;
import com.sparta.tdd.domain.order.dto.OrderSearchOptionDto;
import com.sparta.tdd.domain.order.dto.OrderStatusRequestDto;
import com.sparta.tdd.domain.order.enums.OrderStatus;
import com.sparta.tdd.domain.order.service.OrderService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<Page<OrderResponseDto>> getOrders(
        @ModelAttribute @Valid OrderSearchOptionDto searchOption,
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        Pageable pageable) {
        Page<OrderResponseDto> responseDtos = orderService.getOrders(
            userDetails,
            pageable,
            searchOption
        );

        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrder(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable UUID orderId) {

        OrderResponseDto responseDto = orderService.getOrder(
            userDetails,
            orderId
        );

        return ResponseEntity.ok(responseDto);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody @Valid OrderRequestDto reqDto) {
        OrderResponseDto resDto = orderService.createOrder(
            userDetails,
            reqDto
        );

        URI location = URI.create("/v1/orders/" + resDto.id());

        return ResponseEntity
            .created(location)
            .body(resDto);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER', 'OWNER')")
    @PatchMapping("/{orderId}/next-status")
    public ResponseEntity<OrderResponseDto> nextOrderStatus(
        @PathVariable UUID orderId,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        OrderResponseDto res = orderService.nextOrderStatus(orderId, userDetails);

        return ResponseEntity
            .ok(res);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    @PatchMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
        @PathVariable UUID orderId,
        @RequestBody @Valid OrderStatusRequestDto reqDto
    ) {
        OrderResponseDto res = orderService.changeOrderStatus(orderId, reqDto);

        return ResponseEntity
            .ok(res);
    }

    @PatchMapping("/cancel/{orderId}")
    public ResponseEntity<OrderResponseDto> cancelOrder(
        @PathVariable UUID orderId,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        OrderResponseDto res = orderService.cancelOrder(orderId, userDetails);

        return ResponseEntity
            .ok(res);
    }
}
