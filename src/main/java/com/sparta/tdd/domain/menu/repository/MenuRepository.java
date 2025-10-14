package com.sparta.tdd.domain.menu.repository;

import com.sparta.tdd.domain.menu.entity.Menu;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MenuRepository extends JpaRepository<Menu, UUID>, MenuRepositoryCustom {

    List<Menu> findAllByStoreId(UUID storeId);

    Optional<Menu> findByIdAndStoreIdAndIsDeletedFalse(UUID menuId, UUID storeId);

    List<Menu> findAllByStoreIdAndIsHiddenFalseAndIsDeletedFalse(UUID storeId);

    @Modifying
    @Query("UPDATE Menu m SET m.deletedAt = :deletedAt, m.deletedBy = :deletedBy WHERE m.store.id IN :storeIds AND m.deletedAt IS NULL")
    void bulkSoftDeleteByStoreIds(
        @Param("storeIds") List<UUID> storeIds,
        @Param("deletedAt") LocalDateTime deletedAt,
        @Param("deletedBy") Long deletedBy
    );

    /**
     * DTO로부터 전달받은 메뉴 ID들중 해당 가게에 존재하고, 숨김처리 되지않은 Menu 엔티티들을 조회
     *
     * @param storeId        가게 ID
     * @param menuIdsFromDto DTO로부터 전달받은 메뉴 ID들
     * @return List<Menu> Entity List
     */
    @Query("""
        SELECT m
        FROM Menu m
        WHERE m.id IN :menuIdsFromDto
            AND m.store.id = :storeId
            AND m.isHidden = false
        """)
    List<Menu> findAllVaildMenuIds(Set<UUID> menuIdsFromDto, UUID storeId);
}
