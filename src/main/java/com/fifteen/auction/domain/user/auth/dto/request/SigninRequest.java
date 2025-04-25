package com.fifteen.auction.domain.user.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SigninRequest {

    private String email;
    private String password;
}
