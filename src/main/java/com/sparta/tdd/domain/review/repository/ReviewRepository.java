package com.sparta.tdd.domain.review.repository;

import com.sparta.tdd.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // 삭제되지 않은 리뷰 조회
    @Query("SELECT r FROM Review r WHERE r.reviewId = :reviewId AND r.deletedAt IS NULL")
    Optional<Review> findByIdAndNotDeleted(@Param("reviewId") UUID reviewId);

    // 특정 유저의 삭제되지 않은 리뷰 목록 조회
    @Query("SELECT r FROM Review r WHERE r.userId = :userId AND r.deletedAt IS NULL")
    List<Review> findByUserIdAndNotDeleted(@Param("userId") Long userId);

    // 특정 가게의 삭제되지 않은 리뷰 목록 조회
    @Query("SELECT r FROM Review r WHERE r.storeId = :storeId AND r.deletedAt IS NULL")
    List<Review> findByStoreIdAndNotDeleted(@Param("storeId") UUID storeId);

    // 특정 가게의 평균 평점 조회
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.storeId = :storeId AND r.deletedAt IS NULL")
    Double findAverageRatingByStoreId(@Param("storeId") UUID storeId);

    // 모든 삭제되지 않은 리뷰 조회
    @Query("SELECT r FROM Review r WHERE r.deletedAt IS NULL")
    List<Review> findAllNotDeleted();
}