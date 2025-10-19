package com.sparta.tdd.domain.address.entity;

import com.sparta.tdd.global.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

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
    private String jibunAddress;
    @Comment("도로명주소")
    @Column(name = "road_address", nullable = false)
    private String roadAddress;
    @Comment("상세주소")
    @Column(name = "detail_address", nullable = false)
    private String detailAddress;
    @Comment("위도")
    @Column(name = "latitude", nullable = false)
    private Double latitude;
    @Comment("경도")
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Builder
    public BaseAddress(String jibunAddress, String roadAddress, String detailAddress, Double latitude, Double longitude) {
        this.jibunAddress = jibunAddress;
        this.roadAddress = roadAddress;
        this.detailAddress = detailAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void updateBaseAddress(String jibunAddress, String roadAddress, String detailAdress, String latitude, String longitude) {
        this.jibunAddress = jibunAddress;
        this.roadAddress = roadAddress;
        this.detailAddress = detailAdress;
        this.latitude = Double.valueOf(latitude);
        this.longitude = Double.valueOf(longitude);
    }
}
