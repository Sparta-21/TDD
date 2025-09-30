package com.sparta.tdd.domain.review.entity;

import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.global.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

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
    private UUID id;

    @Comment("리뷰 평점")
    @Column(nullable = false)
    private Integer rating;

    @Comment("리뷰 이미지")
    @Column(name = "image_url")
    private String imageUrl;

    @Comment("리뷰 내용")
    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    public void updateContent(Integer rating, String imageUrl, String content) {
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.content = content;
    }
}
