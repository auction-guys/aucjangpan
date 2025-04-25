package com.fifteen.auction.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserUpdatePasswordRequest {

    private String oldPassword;
    private String newPassword;
}
