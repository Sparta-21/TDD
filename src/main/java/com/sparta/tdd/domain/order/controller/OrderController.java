package com.sparta.tdd.domain.order.controller;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.order.dto.OrderRequestDto;
import com.sparta.tdd.domain.order.dto.OrderResponseDto;
import com.sparta.tdd.domain.order.dto.OrderSearchOptionDto;
import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.enums.OrderStatus;
import com.sparta.tdd.domain.order.service.OrderService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    // 동적검색이 가능해야 한다
    @GetMapping
    public ResponseEntity<Page<OrderResponseDto>> getOrders(
        @ModelAttribute @Valid OrderSearchOptionDto searchOption,
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PageableDefault Pageable pageable) {
        log.info("Controller.getOrders 요청 수신");

        Page<OrderResponseDto> responseDtos = orderService.getOrders(
            userDetails.getUserId(),
            pageable,
            searchOption
        );

        log.info("Controller.getOrders 요청 종료");
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrder(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable UUID orderId) {
        log.info("Controller.getOrder 요청수신");

        OrderResponseDto responseDto = orderService.getOrder(userDetails.getUserId(), orderId);

        log.info("Controller.getOrder 요청 종료");
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody @Valid OrderRequestDto reqDto) {
        log.info("Controller.createOrder 요청수신");
        OrderResponseDto resDto = orderService.createOrder(userDetails.getUserId(), reqDto);

        URI location = URI.create("/v1/orders/" + resDto.id());

        log.info("Controller.createOrder 요청종료, Created URI: {}, ", location);
        return ResponseEntity
            .created(location)
            .body(resDto);
    }
}
