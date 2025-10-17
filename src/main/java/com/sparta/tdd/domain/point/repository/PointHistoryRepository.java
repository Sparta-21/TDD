package com.sparta.tdd.domain.point.repository;

import com.sparta.tdd.domain.point.entity.PointHistory;
import com.sparta.tdd.domain.point.enums.PointType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    boolean existsByReferenceIdAndTypeAndDeletedAtIsNull(UUID referenceId, PointType type);

    Optional<PointHistory> findByReferenceIdAndTypeAndDeletedAtIsNull(UUID paymentId,
        PointType pointType);
}
