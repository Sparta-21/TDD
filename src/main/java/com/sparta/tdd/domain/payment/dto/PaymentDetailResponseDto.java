package com.sparta.tdd.domain.payment.dto;

import com.sparta.tdd.domain.orderMenu.entity.OrderMenu;
import com.sparta.tdd.domain.payment.entity.Payment;
import com.sparta.tdd.domain.store.entity.Store;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PaymentDetailResponseDto(
    String paymentNumber,
    Long price,
    String cardCompany,
    String cardNumber,
    LocalDateTime processedAt,
    RestaurantInfo restaurant,
    List<OrderItemInfo> orderItem
) {

    public static PaymentDetailResponseDto from(Payment payment) {
        RestaurantInfo restaurantInfo = RestaurantInfo.from(payment.getOrder().getStore());
        List<OrderItemInfo> orderItems = OrderItemInfo.fromList(payment.getOrder().getOrderMenuList());

        return new PaymentDetailResponseDto(
            payment.getNumber(),
            payment.getAmount(),
            payment.getCardCompany().getDescription(),
            maskCardNumber(payment.getCardNumber()),
            payment.getProcessedAt(),
            restaurantInfo,
            orderItems
        );
    }

    private static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return cardNumber;
        }

        int length = cardNumber.length();
        String firstFour = cardNumber.substring(0, 4);
        String lastFour = cardNumber.substring(length - 4);
        int middleLength = length - 8;

        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < middleLength; i++) {
            if (i > 0 && i % 4 == 0) {
                masked.append(" ");
            }
            masked.append("*");
        }

        return firstFour + " " + masked + " " + lastFour;
    }

    public record RestaurantInfo(
        UUID id,
        String storeName
    ) {

        public static RestaurantInfo from(Store store) {
            return new RestaurantInfo(
                store.getId(),
                store.getName()
            );
        }
    }

    public record OrderItemInfo(
        UUID id,
        String menuName,
        Integer quantity,
        Integer price,
        Integer totalPrice
    ) {

        public static OrderItemInfo from(OrderMenu orderMenu) {
            return new OrderItemInfo(
                orderMenu.getId(),
                orderMenu.getMenu().getName(),
                orderMenu.getQuantity(),
                orderMenu.getPrice(),
                orderMenu.getPrice() * orderMenu.getQuantity()
            );
        }

        public static List<OrderItemInfo> fromList(List<OrderMenu> orderMenuList) {
            return orderMenuList.stream()
                .map(OrderItemInfo::from)
                .toList();
        }
    }
}
