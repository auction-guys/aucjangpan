package com.fifteen.auction.domain.user.auth.dto.request;

import lombok.Getter;

@Getter
public class SigninRequest {

    private String email;
    private String password;
}
