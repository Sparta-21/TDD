package com.sparta.tdd.domain.menu.repository;

import com.sparta.tdd.domain.menu.entity.Menu;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, UUID> {
    
}
