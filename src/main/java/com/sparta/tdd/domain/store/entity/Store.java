package com.sparta.tdd.domain.store.entity;

import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.global.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Entity
@Table(name = "p_store")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseEntity {

    @Id
    @GeneratedValue
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

    @Builder.Default
    @Column(name = "avg_rating",precision = 2, scale = 1)
    private BigDecimal avgRating = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "review_count")
    private Integer reviewCount = 0;

    public void updateName(String updatedName) {
        this.name = updatedName;
    }
}
