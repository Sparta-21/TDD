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

@Getter
@Table(name = "p_address")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Comment("주소ID")
    @Column(name = "address_id", nullable = false)
    private UUID id;

    @Comment("기본주소")
    @Column(name = "address", nullable = false)
    private String address;
    @Comment("도로명주소")
    @Column(name = "road_address")
    private String roadAddress;
    @Comment("상세주소")
    private String detailAddress;
    @Comment("주소별칭")
    private String alias;
    @Comment("위도")
    private BigDecimal latitude;
    @Comment("경도")
    private BigDecimal longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Address(String address, String roadAddress, String detailAddress, String alias, BigDecimal latitude, BigDecimal longitude) {
        this.address = address;
        this.roadAddress = roadAddress;
        this.detailAddress = detailAddress;
        this.alias = alias;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
