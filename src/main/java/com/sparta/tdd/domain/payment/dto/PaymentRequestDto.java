package com.sparta.tdd.domain.payment.dto;

import com.sparta.tdd.domain.payment.enums.CardCompany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record PaymentRequestDto(
    @NotNull(message = "주문 ID는 필수입니다.")
    UUID orderId,

    @NotNull(message = "카드사는 필수입니다.")
    CardCompany cardCompany,

    @NotBlank(message = "카드번호는 필수입니다.")
    @Pattern(regexp = "^[0-9]{16}$", message = "카드번호는 16자리 숫자여야 합니다.")
    String cardNumber
) {

}