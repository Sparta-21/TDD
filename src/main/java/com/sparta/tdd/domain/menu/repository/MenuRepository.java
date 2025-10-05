package com.sparta.tdd.domain.menu.repository;

import com.sparta.tdd.domain.menu.entity.Menu;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MenuRepository extends JpaRepository<Menu, UUID>, MenuRepositoryCustom {

    List<Menu> findAllByStoreId(UUID storeId);

    Optional<Menu> findByStoreIdAndId(UUID storeId, UUID menuId);

    List<Menu> findAllByStoreIdAndIsHiddenFalse(UUID storeId);

    @Modifying
    @Query("UPDATE Menu m SET m.deletedAt = :deletedAt, m.deletedBy = :deletedBy WHERE m.store.id IN :storeIds AND m.deletedAt IS NULL")
    void bulkSoftDeleteByStoreIds(
        @Param("storeIds") List<UUID> storeIds,
        @Param("deletedAt") LocalDateTime deletedAt,
        @Param("deletedBy") Long deletedBy
    );
}
