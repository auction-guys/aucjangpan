package com.fifteen.auction.domain.user.auth.dto.response;

import lombok.Getter;

@Getter
public class OAuthUserInfo {
    private String sub;
    private String email;
    private String name;
}
