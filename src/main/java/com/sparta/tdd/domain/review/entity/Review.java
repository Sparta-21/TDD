package com.sparta.tdd.domain.review.entity;

import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.global.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.util.UUID;

@Entity
@Table(name = "p_review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Comment("리뷰ID")
    @Column(name = "review_id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Comment("리뷰 평점")
    @Column(nullable = false)
    private Integer rating;

    @Comment("리뷰 이미지")
    @Column(name = "image_url")
    private String imageUrl;

    @Comment("리뷰 내용")
    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder
    public Review(User user, Store store, Order order, Integer rating, String imageUrl, String content) {
        this.user = user;
        this.store = store;
        this.order = order;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.content = content;
    }

    public void updateContent(Integer rating, String imageUrl, String content) {
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.content = content;
    }

    public Long getUserId(){
        Long userId = this.user.getId();
        return userId;
    }

    public UUID getStoreId(){
        UUID storeId = this.store.getId();
        return storeId;
    }

    public UUID getOrderId(){
        UUID orderId = this.order.getId();
        return orderId;
    }


}