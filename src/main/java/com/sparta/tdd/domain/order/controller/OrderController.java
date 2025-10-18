package com.sparta.tdd.domain.order.controller;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.order.dto.OrderRequestDto;
import com.sparta.tdd.domain.order.dto.OrderResponseDto;
import com.sparta.tdd.domain.order.dto.OrderSearchOptionDto;
import com.sparta.tdd.domain.order.dto.OrderStatusRequestDto;
import com.sparta.tdd.domain.order.enums.OrderStatus;
import com.sparta.tdd.domain.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
    name = "주문",
    description = "주문 생성, 조회, 상태 변경 및 취소 API"
)
public class OrderController {
    private final OrderService orderService;

    @Operation(
        summary = "주문 검색",
        description = """
            SearchOption 을 받아 주문을 조회합니다.\n
            페이징은 10, 30, 50 이 지원되며 범위를 벗어난 페이징 요청은 10으로 고정 됩니다.\n
            검색은 시작일 ~ 종료일, 주문한 사용자Id, 주문 상태, 가게Id
            를 사용해서 검색할 수 있고.\n
            검색 조건을 입력하지 않으면 해당 조건은 무시됩니다.
            """
    )
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

    @Operation(
        summary = "주문 단건 조회",
        description = """
           주문Id 를 받아 주문 내역을 조회합니다.\n
           관리자가 아닐경우 로그인 된 사용자의 주문만 조회가 가능합니다.\n
           관리자의 경우 어떤 주문이든 조회가 가능합니다.\n
           """
    )
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

    @Operation(
        summary = "주문 생성",
        description = """
            CUSTOMER 권한을 가진 사용자만 접근 가능합니다.\n
            요청으로 들어온 메뉴들에 대한 검증을 수행합니다.\n
            요청한 메뉴가 가게에 없다면 MENU_INVALID_INFO 예외를 발생시킵니다.\n
            응답 헤더에 생성된 주문의 URI 가 포함되어 반환됩니다.
            """
    )
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

    @Operation(
        summary = "주문 상태를 다음 단계로 변경",
        description = """
            MANAGER, MASTER, OWNER 권한을 가진 사용자만 접근이 가능합니다.\n
            지정한 주문의 현재 상태를 검증한 후, 다음 상태로 변경하여 반환합니다.\n
            상태 전환 규칙을 위반할 경우 예외가 발생합니다.
            """
    )
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

    @Operation(
        summary = "주문의 상태를 임의 변경",
        description = """
            MANAGER, MASTER 권한을 가진 사용자만 접근이 가능합니다.\n
            상태 전환 규칙과 관계 없이 원하는 상태로 변경이 가능합니다.
            """
    )
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

    @Operation(
        summary = "주문 취소",
        description = """
            공통적으로 생성한지 5분 이전의 주문만 취소가 가능합니다.\n
            5분이 지난 주문 취소 요청시 ORDER_CANCELLATION_NOT_ALLOWED 예외가 발생합니다.\n
            MANAGER, MASTER 권한을 가진 사용자는 모든 사용자의 주문이 취소 가능합니다.\n
            OWNER 권한을 가진 사용자는 자신 가게의 주문을 취소 가능합니다.\n
            CUSTOMER 권한을 가진 사용자는 자신이 생성한 주문만 취소 가능합니다.
            """
    )
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
