package com.sparta.tdd.domain.address.repository;

import com.sparta.tdd.domain.address.entity.UserAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {
    @Query("SELECT ua FROM UserAddress ua WHERE ua.user.id = :userId AND ua.deletedAt IS NULL")
    List<UserAddress> findAllByUserIdAndDeletedIsNot(Long userId);

    @Query("SELECT ua FROM UserAddress ua WHERE ua.deletedAt IS NULL")
    Page<UserAddress> findAllUserAddressByDeletedIsNot(Pageable pageable);
}
