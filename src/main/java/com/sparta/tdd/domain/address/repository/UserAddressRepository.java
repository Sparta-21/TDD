package com.sparta.tdd.domain.address.repository;

import com.sparta.tdd.domain.address.entity.Address;
import com.sparta.tdd.domain.address.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {
}
