package com.fifteen.auction.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserUpdateRequest {

    private String email;
    private String nickname;
    private String address;
}
