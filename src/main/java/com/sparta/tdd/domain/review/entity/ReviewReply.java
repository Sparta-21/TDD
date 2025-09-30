package com.sparta.tdd.domain.review.entity;

import com.sparta.tdd.global.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.util.UUID;

@Entity
@Table(name = "p_review_reply")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewReply extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Comment("답글ID")
    @Column(name = "reply_id", nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false, unique = true)
    private Review review;

    @Comment("답글 내용")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Comment("가게 소유자 ID")
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Builder
    public ReviewReply(Review review, String content, Long ownerId) {
        this.review = review;
        this.content = content;
        this.ownerId = ownerId;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public UUID getReviewId() {
        return this.review.getId();
    }
}