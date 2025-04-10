package com.fifteen.auction.domain.user.auth.dto.request;

import lombok.Getter;

@Getter
public class WithdrawRequest {

    private String email;
    private String password;
}
