package com.sparta.tdd.domain.review.entity;

import com.sparta.tdd.global.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.util.UUID;

@Entity
@Table(name = "p_review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Comment("리뷰ID")
    @Column(name = "review_id", nullable = false)
    private UUID reviewId;

    @Comment("회원ID")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Comment("음식점ID")
    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Comment("주문ID")
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Comment("리뷰 평점")
    @Column(nullable = false)
    private Integer rating;

    @Comment("리뷰 이미지")
    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Comment("리뷰 내용")
    @Column(columnDefinition = "TEXT")
    private String content;

    public void updateContent(Integer rating, String imageUrl, String content) {
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.content = content;
    }
}
