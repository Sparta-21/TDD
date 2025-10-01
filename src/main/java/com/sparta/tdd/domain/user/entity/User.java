package com.sparta.tdd.domain.user.entity;

import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.global.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_user")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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

    @OneToMany(mappedBy = "user")
    private List<Store> stores = new ArrayList<>();

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateAuthority(UserAuthority authority) {
        this.authority = authority;
    }

    public void addStore(Store store) {
        this.stores.add(store);
        store.updateUser(this);
    }

    public boolean isOwnerLevel() {
        return EnumSet.of(UserAuthority.OWNER, UserAuthority.MANAGER, UserAuthority.MASTER)
            .contains(this.authority);
    }

    public boolean isManagerLevel() {
        return EnumSet.of(UserAuthority.MANAGER, UserAuthority.MASTER).contains(this.authority);
    }
}
