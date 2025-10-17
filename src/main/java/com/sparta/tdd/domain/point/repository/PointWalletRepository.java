package com.sparta.tdd.domain.point.repository;

import com.sparta.tdd.domain.point.entity.PointWallet;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointWalletRepository extends JpaRepository<PointWallet, Long> {

    Optional<PointWallet> findByUserId(Long id);
}
