package com.sparta.tdd.domain.address.entity;

import com.sparta.tdd.global.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.UUID;

@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BaseAddress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "address_id", nullable = false, updatable = false)
    private UUID id;
    @Comment("기본주소")
    @Column(name = "address", nullable = false)
    private String address;
    @Comment("도로명주소")
    @Column(name = "road_address")
    private String roadAddress;
    @Comment("상세주소")
    private String detailAddress;
    @Comment("위도")
    private BigDecimal latitude;
    @Comment("경도")
    private BigDecimal longitude;

    @Builder
    public BaseAddress(String address, String roadAddress, String detailAddress, BigDecimal latitude, BigDecimal longitude) {
        this.address = address;
        this.roadAddress = roadAddress;
        this.detailAddress = detailAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
