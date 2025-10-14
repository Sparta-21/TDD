package com.sparta.tdd.domain.coupon.controller;

import com.sparta.tdd.domain.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

}
