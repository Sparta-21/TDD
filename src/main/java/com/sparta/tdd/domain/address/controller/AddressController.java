package com.sparta.tdd.domain.address.controller;

import com.sparta.tdd.domain.address.dto.*;
import com.sparta.tdd.domain.address.service.AddressService;
import com.sparta.tdd.domain.address.service.NaverMapService;
import com.sparta.tdd.domain.auth.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(
            summary = "주소 조회",
            description = """
                    외부 api를 사용하여 입력한 주소의 지번주소, 도로명주소, 위도, 경도를 조회합니다.
                    """)
    @GetMapping("/{address}")
    public ResponseEntity<Page<AddressResponseDto>> getAddress(@PathVariable("address") String address,
                                                         Pageable pageable) {
        Page<AddressResponseDto> addressPage = naverMapService.getAddress(address, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(addressPage);
    }

    @Operation(
            summary = "가게 주소 등록",
            description = """
                    지번주소, 도로명주소, 위도, 경도를 입력하여 가게의 주소를 등록합니다.
                    가게의 주인만 주소를 등록할 수 있습니다.
                    """
    )
    @PreAuthorize("hasAnyRole('OWNER')")
    @PostMapping("/store/{storeId}")
    public ResponseEntity<StoreAddressResponseDto> createStoreAddress(@PathVariable("storeId") UUID storeId,
                                                                      @RequestBody StoreAddressRequestDto requestDto) {
        StoreAddressResponseDto responseDto = addressService.createStoreAddress(storeId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(
            summary = "유저 주소 등록",
            description = """
                    지번주소, 도로명주소, 별칭, 위도, 경도를 입력하여 본인의 주소를 등록할 수 있습니다.
                    자신의 주소만 등록할 수 있습니다.
                    """
    )
    @PostMapping("/user")
    public ResponseEntity<UserAddressResponseDto> createUserAddress(@RequestBody UserAddressRequestDto requestDto,
                                                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserAddressResponseDto responseDto = addressService.createUserAddress(userDetails.getUserId(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(
            summary = "주소 목록 조회",
            description = """
                    자신의 주소 목록을 조회합니다.
                    """
    )
    @GetMapping("/user")
    public ResponseEntity<List<UserAddressResponseDto>> getUserAddresses(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<UserAddressResponseDto> responseDtoList = addressService.getUserAddressByUserId(userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(responseDtoList);
    }

    @Operation(
            summary = "가게 주소 목록 조회",
            description = """
                    모든 가게의 주소만을 조회합니다.
                    MASTER 권한을 가진 유저만 조회할 수 있습니다.
                    """
    )
    @PreAuthorize("hasAnyRole('MASTER')")
    @GetMapping("/store")
    public ResponseEntity<Page<StoreAddressResponseDto>> getAllStoreAddress(Pageable pageable) {
        Page<StoreAddressResponseDto> allStoreAddress = addressService.getAllStoreAddress(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(allStoreAddress);
    }

    @Operation(
            summary = "회원 주소 목록 조회",
            description = """
                    모든 회원의 주소를 조회합니다.
                    MASTER 권한을 가진 유저만 조회할 수 있습니다.
                    """
    )
    @PreAuthorize("hasAnyRole('MASTER')")
    @GetMapping("/user/all")
    public ResponseEntity<Page<UserAddressResponseDto>> getAllUserAddress(Pageable pageable) {
        Page<UserAddressResponseDto> allUserAddress = addressService.getAllUserAddress(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(allUserAddress);
    }

    @Operation(
            summary = "가게 주소 업데이트",
            description = """
                   지번주소, 도로명주소, 위도, 경도를 입력하여 가게의 주소를 변경합니다.
                   해당 가게의 주인만이 주소를 변경할 수 있습니다. 
                   """
    )
    @PreAuthorize("hasAnyRole('OWNER')")
    @PatchMapping("/store/{addressId}")
    public ResponseEntity<StoreAddressResponseDto> updateStoreAddress(@PathVariable("addressId") UUID addressId, StoreAddressRequestDto requestDto) {
        StoreAddressResponseDto responseDto = addressService.updateStoreAddress(addressId, requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @Operation(
            summary = "회원 주소 업데이트",
            description = """
                    지번주소, 도로명주소, 별칭, 위도, 경도를 입력하여 회원의 주소를 변경합니다.
                    """
    )
    @PatchMapping("/user/{addressId}")
    public ResponseEntity<UserAddressResponseDto> updateUserAddress(@PathVariable("addressId") UUID addressId, UserAddressRequestDto requestDto) {
        UserAddressResponseDto responseDto = addressService.updateUserAddress(addressId, requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @Operation(
            summary = "가게 주소 삭제",
            description = """
                    가게의 주소를 삭제합니다.
                    """
    )
    @PreAuthorize("hasAnyRole('OWNER')")
    @DeleteMapping("/store/{addressId}")
    public ResponseEntity<Void> deleteStoreAddress(@PathVariable("addressId") UUID addressId,
                                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        addressService.deleteStoreAddress(addressId, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "회원 주소 삭제",
            description = """
                    회원 주소를 삭제합니다. 자신의 주소만 삭제할 수 있습니다.
                    """
    )
    @DeleteMapping("/user/{addressId}")
    public ResponseEntity<Void> deleteUserAddress(@PathVariable("addressId") UUID addressId,
                                                  @AuthenticationPrincipal UserDetailsImpl userDetails) {
        addressService.deleteUserAddress(addressId, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "회원 대표 주소 갱신",
            description = """
                    회원의 대표 주소를 갱신합니다. 자신의 주소만 갱신할 수 있습니다.
                    """
    )
    @PatchMapping("/user/{addressId}/primary")
    public ResponseEntity<Void> updatePrimaryUserAddress(@PathVariable("addressId") UUID addressId,
                                                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        addressService.choicePrimaryUserAddress(addressId, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
