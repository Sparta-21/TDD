package com.sparta.tdd.domain.auth.service;

import com.sparta.tdd.domain.menu.repository.MenuRepository;
import com.sparta.tdd.domain.order.repository.OrderRepository;
import com.sparta.tdd.domain.orderMenu.repository.OrderMenuRepository;
import com.sparta.tdd.domain.payment.repository.PaymentRepository;
import com.sparta.tdd.domain.review.repository.ReviewReplyRepository;
import com.sparta.tdd.domain.review.repository.ReviewRepository;
import com.sparta.tdd.domain.store.repository.StoreRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WithdrawalDataCleanService {

    private final ReviewRepository reviewRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final OrderMenuRepository orderMenuRepository;

    public void deleteOwnerRelatedData(Long userId, LocalDateTime deletedAt) {
        List<UUID> storeIds = storeRepository.findStoreIdsByUserIdAndDeletedAtIsNull(userId);
        if (!storeIds.isEmpty()) {
            menuRepository.bulkSoftDeleteByStoreIds(storeIds, deletedAt, userId);
            List<UUID> reviewIds = reviewRepository.findReviewIdsByStoreIds(storeIds);
            if (!reviewIds.isEmpty()) {
                reviewReplyRepository.bulkSoftDeleteByReviewIds(reviewIds, deletedAt, userId);
            }
            reviewRepository.bulkSoftDeleteByStoreIds(storeIds, deletedAt, userId);
        }

        storeRepository.bulkSoftDeleteByUserId(userId, deletedAt, userId);
    }

    public void deleteCommonUserData(Long userId, LocalDateTime deletedAt) {
        paymentRepository.bulkSoftDeleteByUserId(userId, deletedAt, userId);
        List<UUID> orderIds = orderRepository.findOrderIdsByUserIdAndDeletedAtIsNull(userId);
        orderMenuRepository.bulkSoftDeleteByOrderIds(orderIds, deletedAt, userId);
        orderRepository.bulkSoftDeleteByUserId(userId, deletedAt, userId);
    }

}
