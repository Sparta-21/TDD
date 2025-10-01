package com.sparta.tdd.domain.store.repository;

import com.sparta.tdd.domain.store.entity.Store;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {

    Optional<Store> findByName(String name);
}
