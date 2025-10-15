package com.sparta.tdd.domain.address.repository;

import com.sparta.tdd.domain.address.entity.StoreAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

public interface StoreAddressRepository extends JpaRepository<StoreAddress, UUID> {
}
