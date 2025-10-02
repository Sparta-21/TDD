package com.sparta.tdd.domain.menu.repository;

import com.sparta.tdd.domain.menu.entity.Menu;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, UUID> {

    List<Menu> findAllByStoreId(UUID storeId);

    Optional<Menu> findByStoreIdAndId(UUID storeId, UUID menuId);

    List<Menu> findAllByStoreIdAndIsHiddenFalse(UUID storeId);
}
