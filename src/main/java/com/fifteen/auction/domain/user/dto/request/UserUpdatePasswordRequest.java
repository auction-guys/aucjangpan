package com.fifteen.auction.domain.user.dto.request;

import lombok.Getter;

@Getter
public class UserUpdatePasswordRequest {

    private String oldPassword;
    private String newPassword;
}
