package com.sparta.tdd.domain.store.entity;

import com.sparta.tdd.domain.store.dto.StoreRequestDto;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.global.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_store")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "store_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private StoreCategory category;

    @Column(name = "description")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "avg_rating", precision = 2, scale = 1)
    private BigDecimal avgRating;

    @Column(name = "review_count")
    private Integer reviewCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Store(String name, StoreCategory category, String description, String imageUrl,
        User user) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.imageUrl = imageUrl;
        this.avgRating = BigDecimal.ZERO;
        this.reviewCount = 0;
        this.user = user;
    }

    public void updateName(String updatedName) {
        this.name = updatedName;
    }

    public void updateStore(User user, StoreRequestDto requestDto) {
        this.name = requestDto.name();
        this.user = user;
        this.category = requestDto.category();
        this.description = requestDto.description();
    }

    public boolean isOwner(User user) {
        return this.getUser().getId().equals(user.getId());
    }

    public void updateRatingInfo(BigDecimal newAvgRating, Integer newReviewCount) {
        this.avgRating = newAvgRating;
        this.reviewCount = newReviewCount;
    }
}
