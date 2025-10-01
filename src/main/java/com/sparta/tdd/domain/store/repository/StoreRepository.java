package com.sparta.tdd.domain.store.repository;

import com.sparta.tdd.domain.store.entity.Store;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreRepository extends JpaRepository<Store, UUID> {

    @Query("SELECT s FROM Store s WHERE s.id = :storeId AND s.deletedAt IS NULL")
    Optional<Store> findByStoreIdAndNotDeleted(@Param("storeId") UUID storeId);
}
