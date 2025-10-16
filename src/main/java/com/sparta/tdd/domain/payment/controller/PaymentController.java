package com.sparta.tdd.domain.payment.controller;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.payment.dto.PaymentDetailResponseDto;
import com.sparta.tdd.domain.payment.dto.PaymentListResponseDto;
import com.sparta.tdd.domain.payment.dto.PaymentRequestDto;
import com.sparta.tdd.domain.payment.dto.UpdatePaymentStatusRequest;
import com.sparta.tdd.domain.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(
        summary = "고객 본인 결제 내역 확인",
        description = "로그인 된 유저가 고객 권한일 경우, 본인이 결제한 내역을 확인할 수 있습니다."
    )
    @GetMapping("")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<PaymentListResponseDto>> getCustomerPaymentHistory(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PageableDefault Pageable pageable,
        @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(paymentService.getCustomerPaymentHistory(userDetails.getUserId(), pageable, keyword));
    }

    @Operation(
        summary = "가게 결제 내역 확인",
        description = "로그인 된 유저가 가게 사장 권한일 경우, storeId에 해당하는 가게의 결제 내역을 확인할 수 있습니다."
    )
    @GetMapping("/store/{storeId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    public ResponseEntity<Page<PaymentListResponseDto>> getStorePaymentHistory(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable(name = "storeId") UUID storeId,
        Pageable pageable,
        @RequestParam(required = false) String keyword
    ) {
        Page<PaymentListResponseDto> response = paymentService.getStorePaymentHistory(userDetails.getUserId(), storeId,
            pageable,
            keyword);

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "결제 상세 내역 확인",
        description = "해당하는 paymentId에 대한 결제 상세 내역을 확인할 수 있습니다."
    )
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDetailResponseDto> getPaymentHistoryDetail(
        @PathVariable UUID paymentId
    ) {
        PaymentDetailResponseDto response = paymentService.getPaymentHistoryDetail(paymentId);

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "결제 상태 변경",
        description = "관리자 권한일 경우, 결제 상태를 변경할 수 있습니다."
    )
    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    @PatchMapping("/status/{paymentId}")
    public ResponseEntity<Void> changeHistoryStatus(
        @PathVariable UUID paymentId,
        @Valid @RequestBody UpdatePaymentStatusRequest request
    ) {
        paymentService.changePaymentStatus(paymentId, request);

        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "결제 요청",
        description = "해당하는 카드로 결제요청을 할 수 있습니다."
    )
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/request")
    public ResponseEntity<?> requestPayment(
        @Valid @RequestBody PaymentRequestDto request,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        paymentService.requestPayment(userDetails.getUserId(), request);
        return ResponseEntity.ok().build();
    }
}
