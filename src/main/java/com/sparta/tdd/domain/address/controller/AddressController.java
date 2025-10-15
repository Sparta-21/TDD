package com.sparta.tdd.domain.address.controller;

import com.sparta.tdd.domain.address.dto.AddressResponseDto;
import com.sparta.tdd.domain.address.dto.NaverAddress;
import com.sparta.tdd.domain.address.dto.NaverAddressResponse;
import com.sparta.tdd.domain.address.service.AddressService;
import com.sparta.tdd.domain.address.service.NaverMapService;
import com.sparta.tdd.domain.auth.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final NaverMapService naverMapService;

    // 주소 조회
    @GetMapping("/{address}")
    public ResponseEntity<Page<NaverAddress>> getAddress(@PathVariable("address") String address,
                                                         Pageable pageable) {
        Page<NaverAddress> addressPage = naverMapService.getAddress(address, pageable);
        return ResponseEntity.ok(addressPage);
    }


}
