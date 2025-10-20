package com.sparta.tdd.global.aop;

import com.sparta.tdd.domain.payment.entity.Payment;
import com.sparta.tdd.domain.payment.enums.PaymentStatus;
import com.sparta.tdd.domain.point.dto.PointRequest;
import com.sparta.tdd.domain.point.service.PointService;
import com.sparta.tdd.domain.review.dto.response.ReviewResponseDto;
import com.sparta.tdd.domain.review.entity.Review;
import com.sparta.tdd.domain.review.repository.ReviewRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class PointAspect {

    private final PointService pointService;
    private final ReviewRepository reviewRepository;

    private static final double ORDER_POINT_RATE = 0.01;
    private static final long REVIEW_POINT_AMOUNT = 500L;

    @AfterReturning(
        pointcut = "execution(* com.sparta.tdd.domain.payment.service.PaymentResultProcessService.processPaymentResult(..)) && args(payment)",
        argNames = "payment"
    )
    public void processPointsAfterPaymentResult(Payment payment) {
        try {

            User user = payment.getUser();

            if (payment.getStatus() == PaymentStatus.COMPLETED) {

                log.info("결제 완료 payment_Id={}", payment.getId());

                Long totalAmount = payment.getAmount();
                Long earnAmount = (long) Math.floor(totalAmount * ORDER_POINT_RATE);

                pointService.earnPoints(PointRequest.forPayment(
                    user,
                    payment.getId(),
                    earnAmount,
                    "결제 완료 적립 (결제번호 번호: " + payment.getId() + ")"
                ));
            }

            if (payment.getStatus() == PaymentStatus.CANCELLED) {
                log.info("취소 완료 payment_Id={}", payment.getId());

                pointService.losePoints(user, payment.getId());
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(ErrorCode.POINT_PROCESSING_FAILED);
        }
    }

    @AfterReturning(
        pointcut = "execution(* com.sparta.tdd.domain.review.service.ReviewService.createReview(..))",
        returning = "result"
    )
    public void earnPointsAfterReviewCreation(ReviewResponseDto result) {
        try {
            log.info("리뷰 작성 완료", result.reviewId());

            Review review = reviewRepository.findById(result.reviewId())
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

            User user = review.getUser();
            Long earnAmount = REVIEW_POINT_AMOUNT;

            pointService.earnPoints(PointRequest.forReview(
                user,
                review.getId(),
                earnAmount,
                "리뷰 적립 (리뷰 번호: " + review.getId() + ")"
            ));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(ErrorCode.POINT_PROCESSING_FAILED);
        }
    }
}
