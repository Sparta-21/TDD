package com.sparta.tdd.domain.address.service;

import com.sparta.tdd.domain.address.dto.*;
import com.sparta.tdd.domain.address.entity.StoreAddress;
import com.sparta.tdd.domain.address.entity.UserAddress;
import com.sparta.tdd.domain.address.repository.StoreAddressRepository;
import com.sparta.tdd.domain.address.repository.UserAddressRepository;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.repository.StoreRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.repository.UserRepository;
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AddressService {

    private final StoreAddressRepository storeAddressRepository;
    private final UserAddressRepository userAddressRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    // 가게 주소 등록
    @Transactional
    public StoreAddressResponseDto createStoreAddress(UUID storeId, StoreAddressRequestDto requestDto) {
        Store store = findStore(storeId);
        StoreAddress storeAddress = StoreAddressRequestDto.toEntity(requestDto, store);
        storeAddressRepository.save(storeAddress);
        return StoreAddressResponseDto.from(storeAddress);
    }

    // 회원 주소 등록
    @Transactional
    public UserAddressResponseDto createUserAddress(Long userId, UserAddressRequestDto requestDto) {
        User user = findUser(userId);
        UserAddress userAddress = UserAddressRequestDto.toEntity(requestDto, user);
        userAddressRepository.save(userAddress);
        return UserAddressResponseDto.from(userAddress);
    }
    // 회원 주소 목록 조회
    public List<UserAddressResponseDto> getUserAddressByUserId(Long userId) {
        List<UserAddress> userAddressList = userAddressRepository.findAllByUserIdAndDeletedIsNot(userId);
        return userAddressList.stream()
                .map(UserAddressResponseDto::from)
                .toList();
    }
    // 모든 가게 주소 페이징 조회
    public Page<StoreAddressResponseDto> getAllStoreAddress(Pageable pageable) {
        Page<StoreAddress> storeAddressPage = storeAddressRepository.findAllStoreAddressByDeletedIsNot(pageable);
        return storeAddressPage.map(StoreAddressResponseDto::from);
    }
    // 모든 회원 주소 페이징 조회
    public Page<UserAddressResponseDto> getAllUserAddress(Pageable pageable) {
        Page<UserAddress> userAddressPage = userAddressRepository.findAllUserAddressByDeletedIsNot(pageable);
        return userAddressPage.map(UserAddressResponseDto::from);
    }
    // 주소 수정
    @Transactional
    public UserAddressResponseDto updateUserAddress(UUID addressId, UserAddressRequestDto requestDto) {
        UserAddress userAddress = findUserAddress(addressId);
        userAddress.updateUserAddress(requestDto.jibunAddress(), requestDto.roadAddress(), requestDto.detailAddress(),
                requestDto.alias(), requestDto.latitude(), requestDto.longitude());
        return UserAddressResponseDto.from(userAddress);
    }
    @Transactional
    public StoreAddressResponseDto updateStoreAddress(UUID addressId, StoreAddressRequestDto requestDto) {
        StoreAddress storeAddress = findStoreAddress(addressId);
        storeAddress.updateBaseAddress(requestDto.jibunAddress(), requestDto.roadAddress(), requestDto.detailAddress(),
                requestDto.latitude(), requestDto.longitude());
        return StoreAddressResponseDto.from(storeAddress);
    }
    // 가게 주소 삭제
    @Transactional
    public void deleteStoreAddress(UUID addressId, Long userId) {
        StoreAddress storeAddress = findStoreAddress(addressId);
        User user = findUser(userId);

        storeAddress.validateOwner(user);
        storeAddress.delete(userId);
    }
    // 회원 주소 삭제
    @Transactional
    public void deleteUserAddress(UUID addressId, Long userId) {
        UserAddress userAddress = findUserAddress(addressId);

        userAddress.validateUser(userId);
        userAddress.delete(userId);
    }
    // 회원 대표 주소 설정
    @Transactional
    public void choicePrimaryUserAddress(UUID addressId, Long userId) {
        List<UserAddress> userAddressList = userAddressRepository.findAllByUserIdAndDeletedIsNot(userId);
        userAddressList.forEach(userAddress -> {
            if (userAddress.getIsPrimary()) {
                userAddress.updatePrimary();
            }
            if (userAddress.getId().equals(addressId)) {
                userAddress.updatePrimary();
            }
        });
    }
    private Store findStore(UUID storeId) {
        return storeRepository.findByStoreIdAndNotDeleted(storeId)
                .orElseThrow(() -> {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        });
    }
    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        });
    }
    private UserAddress findUserAddress(UUID addressId) {
        return userAddressRepository.findById(addressId)
                .orElseThrow(() -> {
                    throw new BusinessException(ErrorCode.ADDRESS_NOT_FOUND);
                });
    }
    private StoreAddress findStoreAddress(UUID addressId) {
        return storeAddressRepository.findById(addressId)
                .orElseThrow(() -> {
            throw new BusinessException(ErrorCode.ADDRESS_NOT_FOUND);
        });
    }
}
