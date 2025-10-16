package com.sparta.tdd.domain.store.repository;

import com.sparta.tdd.domain.store.entity.Store;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreRepository extends JpaRepository<Store, UUID>, StoreRepositoryCustom {

    @Query("SELECT s FROM Store s WHERE s.id = :storeId AND s.deletedAt IS NULL")
    Optional<Store> findByStoreIdAndNotDeleted(@Param("storeId") UUID storeId);

    Optional<Store> findByName(String name);

    @Modifying
    @Query("UPDATE Store s SET s.deletedAt = :deletedAt, s.deletedBy = :deletedBy WHERE s.user.id = :userId AND s.deletedAt IS NULL")
    void bulkSoftDeleteByUserId(
        @Param("userId") Long userId,
        @Param("deletedAt") LocalDateTime deletedAt,
        @Param("deletedBy") Long deletedBy);

    @Query("SELECT s.id FROM Store s WHERE s.user.id = :userId AND s.deletedAt IS NULL")
    List<UUID> findStoreIdsByUserIdAndDeletedAtIsNull(Long userId);

    boolean existsByIdAndUserIdAndDeletedAtIsNull(UUID storeId, Long userId);
}
