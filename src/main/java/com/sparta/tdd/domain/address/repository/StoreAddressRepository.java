package com.sparta.tdd.domain.address.repository;

import com.sparta.tdd.domain.address.entity.StoreAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface StoreAddressRepository extends JpaRepository<StoreAddress, UUID> {
    @Query("SELECT sa FROM StoreAddress sa WHERE sa.deletedAt IS NULL")
    Page<StoreAddress> findAllStoreAddressByDeletedIsNot(Pageable pageable);
}
