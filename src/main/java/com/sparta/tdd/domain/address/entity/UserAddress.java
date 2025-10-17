package com.sparta.tdd.domain.address.entity;

import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "p_user_address")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAddress extends BaseAddress {

    @Comment("주소별칭")
    @Column(name = "alias")
    private String alias;
    @Column(name = "is_primary")
    private Boolean isPrimary;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public UserAddress(String address, String roadAddress, String detailAddress, String alias, Double latitude, Double longitude, User user) {
        super(address, roadAddress, detailAddress, latitude, longitude);
        this.alias = alias;
        this.user = user;
        this.isPrimary = false;
    }

    public void updateUserAddress(String jibunAddress, String roadAddress, String detailAddress,
                                  String alias, String latitude, String longitude) {
        super.updateBaseAddress(jibunAddress, roadAddress, detailAddress, latitude, longitude);
        updateAlias(alias);
    }
    public void updateAlias(String alias) {
        this.alias = alias;
    }
    public void updatePrimary() {
        this.isPrimary = !isPrimary;
    }
    public void validateUser(Long id) {
        if (!user.isSameId(id)) {
            throw new BusinessException(ErrorCode.ADDRESS_USER_PERMISSION_DENIED);
        }
    }
}
