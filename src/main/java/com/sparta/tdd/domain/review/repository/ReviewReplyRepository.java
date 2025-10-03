package com.sparta.tdd.domain.review.repository;

import com.sparta.tdd.domain.review.entity.ReviewReply;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewReplyRepository extends JpaRepository<ReviewReply, UUID> {

    // 특정 리뷰의 삭제되지 않은 답글 조회
    @Query("SELECT rr FROM ReviewReply rr WHERE rr.review.id = :reviewId AND rr.deletedAt IS NULL")
    Optional<ReviewReply> findByReviewIdAndNotDeleted(@Param("reviewId") UUID reviewId);

    // 여러 리뷰의 삭제되지 않은 답글 목록 조회 (목록 조회용)
    @Query("SELECT rr FROM ReviewReply rr WHERE rr.review.id IN :reviewIds AND rr.deletedAt IS NULL")
    List<ReviewReply> findByReviewIdsAndNotDeleted(@Param("reviewIds") List<UUID> reviewIds);

    @Modifying
    @Query("UPDATE ReviewReply rr SET rr.deletedAt = :now, rr.deletedBy = :userId WHERE rr.review.id IN :reviewIds AND rr.deletedAt IS NULL")
    void bulkSoftDeleteByReviewIds(List<UUID> reviewIds, LocalDateTime now, Long userId);
}