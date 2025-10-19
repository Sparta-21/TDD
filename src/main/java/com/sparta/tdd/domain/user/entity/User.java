package com.sparta.tdd.domain.user.entity;

import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.global.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.EnumSet;

@Getter
@Entity
@Table(name = "p_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long id;

    @Column(name = "username", nullable = false, length = 10, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "authority", nullable = false, length = 20)
    private UserAuthority authority;

    @Builder
    public User(String username, String password, String nickname, UserAuthority authority) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.authority = authority;
    }

    public boolean isSameId(Long id) {
        if (this.id != id) {
            return false;
        }
        return true;
    }
    public boolean isOwnerLevel() {
        return EnumSet.of(UserAuthority.OWNER, UserAuthority.MANAGER, UserAuthority.MASTER)
            .contains(this.authority);
    }

    public boolean isManagerLevel() {
        return EnumSet.of(UserAuthority.MANAGER, UserAuthority.MASTER).contains(this.authority);
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateAuthority(UserAuthority authority) {
        this.authority = authority;
    }
}
