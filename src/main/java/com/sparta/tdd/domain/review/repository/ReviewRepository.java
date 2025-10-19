package com.sparta.tdd.domain.review.repository;

import com.sparta.tdd.domain.review.entity.Review;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // 삭제되지 않은 리뷰 조회
    @Query("SELECT r FROM Review r WHERE r.id = :reviewId AND r.deletedAt IS NULL")
    Optional<Review> findByIdAndNotDeleted(@Param("reviewId") UUID reviewId);

    // 특정 유저의 삭제되지 않은 리뷰 목록 조회
    @Query("SELECT r FROM Review r WHERE r.user.id = :userId AND r.deletedAt IS NULL")
    List<Review> findByUserIdAndNotDeleted(@Param("userId") Long userId);

    // 특정 가게의 삭제되지 않은 리뷰 목록 조회
    @Query("SELECT r FROM Review r WHERE r.store.id = :storeId AND r.deletedAt IS NULL")
    List<Review> findByStoreIdAndNotDeleted(@Param("storeId") UUID storeId);

    @Query("SELECT r FROM Review r WHERE r.store.id = :storeId AND r.deletedAt IS NULL")
    Page<Review> findPageByStoreIdAndNotDeleted(@Param("storeId") UUID storeId, Pageable pageable);

    // 특정 가게의 평균 평점 조회
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.store.id = :storeId AND r.deletedAt IS NULL")
    Double findAverageRatingByStoreId(@Param("storeId") UUID storeId);

    // 모든 삭제되지 않은 리뷰 조회
    @Query("SELECT r FROM Review r WHERE r.deletedAt IS NULL")
    List<Review> findAllNotDeleted();

    @Modifying
    @Query("UPDATE Review r SET r.deletedAt = :deletedAt, r.deletedBy = :deletedBy WHERE r.store.id IN :storeIds AND r.deletedAt IS NULL")
    void bulkSoftDeleteByStoreIds(
        @Param("storeIds") List<UUID> storeIds,
        @Param("deletedAt") LocalDateTime deletedAt,
        @Param("deletedBy") Long deletedBy
    );

    @Modifying
    @Query("UPDATE Review r SET r.deletedAt = :deletedAt, r.deletedBy = :deletedBy WHERE r.user.id = :userId AND r.deletedAt IS NULL")
    void bulkSoftDeleteByUserId(
        @Param("userId") Long userId,
        @Param("deletedAt") LocalDateTime deletedAt,
        @Param("deletedBy") Long deletedBy
    );

    @Query("SELECT r.id FROM Review r WHERE r.store.id IN :storeIds AND r.deletedAt IS NULL")
    List<UUID> findReviewIdsByStoreIds(@Param("storeIds") List<UUID> storeIds);

    @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.order.id = :orderId AND r.deletedAt IS NULL")
    boolean existsByOrderId(@Param("orderId") UUID orderId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.store.id = :storeId AND r.deletedAt IS NULL")
    Long countByStoreIdAndNotDeleted(@Param("storeId") UUID storeId);
}