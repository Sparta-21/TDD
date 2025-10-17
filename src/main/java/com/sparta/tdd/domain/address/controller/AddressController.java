package com.sparta.tdd.domain.address.controller;

import com.sparta.tdd.domain.address.dto.*;
import com.sparta.tdd.domain.address.service.AddressService;
import com.sparta.tdd.domain.address.service.NaverMapService;
import com.sparta.tdd.domain.auth.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final NaverMapService naverMapService;

    // 주소 조회
    @GetMapping("/{address}")
    public ResponseEntity<Page<AddressResponseDto>> getAddress(@PathVariable("address") String address,
                                                         Pageable pageable) {
        Page<AddressResponseDto> addressPage = naverMapService.getAddress(address, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(addressPage);
    }

    // 가게 주소 등록
    @PreAuthorize("hasAnyRole('OWNER')")
    @PostMapping("/store/{storeId}")
    public ResponseEntity<StoreAddressResponseDto> createStoreAddress(@PathVariable("storeId") UUID storeId,
                                                                      @RequestBody StoreAddressRequestDto requestDto) {
        StoreAddressResponseDto responseDto = addressService.createStoreAddress(storeId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
    // 회원 주소 등록
    @PostMapping("/user")
    public ResponseEntity<UserAddressResponseDto> createUserAddress(@RequestBody UserAddressRequestDto requestDto,
                                                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserAddressResponseDto responseDto = addressService.createUserAddress(userDetails.getUserId(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
    // 회원 주소 목록 조회
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    @GetMapping("/user")
    public ResponseEntity<List<UserAddressResponseDto>> getUserAddresses(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<UserAddressResponseDto> responseDtoList = addressService.getUserAddressByUserId(userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(responseDtoList);
    }

    // 모든 가게 주소 페이징 조회
    @PreAuthorize("hasAnyRole('MASTER')")
    @GetMapping("/store")
    public ResponseEntity<Page<StoreAddressResponseDto>> getAllStoreAddress(Pageable pageable) {
        Page<StoreAddressResponseDto> allStoreAddress = addressService.getAllStoreAddress(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(allStoreAddress);
    }
    // 모든 회원 주소 페이징 조회
    @PreAuthorize("hasAnyRole('MASTER')")
    @GetMapping("/user/all")
    public ResponseEntity<Page<UserAddressResponseDto>> getAllUserAddress(Pageable pageable) {
        Page<UserAddressResponseDto> allUserAddress = addressService.getAllUserAddress(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(allUserAddress);
    }
    // 가게 주소 수정
    @PreAuthorize("hasAnyRole('OWNER')")
    @PatchMapping("/store/{addressId}")
    public ResponseEntity<StoreAddressResponseDto> updateStoreAddress(@PathVariable("addressId") UUID addressId, StoreAddressRequestDto requestDto) {
        StoreAddressResponseDto responseDto = addressService.updateStoreAddress(addressId, requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }
    // 회원 주소 수정
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    @PatchMapping("/user/{addressId}")
    public ResponseEntity<UserAddressResponseDto> updateUserAddress(@PathVariable("addressId") UUID addressId, UserAddressRequestDto requestDto) {
        UserAddressResponseDto responseDto = addressService.updateUserAddress(addressId, requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }
    // 가게 주소 삭제
    @PreAuthorize("hasAnyRole('OWNER')")
    @DeleteMapping("/store/{addressId}")
    public ResponseEntity<Void> deleteStoreAddress(@PathVariable("addressId") UUID addressId,
                                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        addressService.deleteStoreAddress(addressId, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    // 회원 주소 단건 삭제
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    @DeleteMapping("/user/{addressId}")
    public ResponseEntity<Void> deleteUserAddress(@PathVariable("addressId") UUID addressId,
                                                  @AuthenticationPrincipal UserDetailsImpl userDetails) {
        addressService.deleteUserAddress(addressId, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    // 회원 대표 주소 변경
    @PatchMapping("/user/{addressId}/primary")
    public ResponseEntity<Void> updatePrimaryUserAddress(@PathVariable("addressId") UUID addressId,
                                                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        addressService.choicePrimaryUserAddress(addressId, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
