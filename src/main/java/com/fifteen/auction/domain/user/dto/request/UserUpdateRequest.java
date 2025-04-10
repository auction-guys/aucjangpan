package com.fifteen.auction.domain.user.dto.request;

import lombok.Getter;

@Getter
public class UserUpdateRequest {

    private String email;
    private String nickname;
    private String address;
}
