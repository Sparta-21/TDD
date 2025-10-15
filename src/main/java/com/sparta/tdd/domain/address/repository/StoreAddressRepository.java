package com.sparta.tdd.domain.address.repository;

import com.sparta.tdd.domain.address.entity.StoreAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreAddressRepository extends JpaRepository<StoreAddress, UUID> {
}
