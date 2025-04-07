package com.fifteen.auction.domain.user.auth.entity;

import lombok.Getter;

@Getter
public class AuthUser {

    private final Long id;
    private final String email;
    private final String nickname;

    public AuthUser(Long id, String email, String nickname) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
    }
}
