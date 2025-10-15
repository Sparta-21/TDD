package com.sparta.tdd.domain.address.entity;

import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.global.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "p_address")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAddress extends BaseAddress {

    @Comment("주소별칭")
    private String alias;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public UserAddress(String address, String roadAddress, String detailAddress, String alias, BigDecimal latitude, BigDecimal longitude) {
        super(address, roadAddress, detailAddress, latitude, longitude);
        this.alias = alias;
    }
}
