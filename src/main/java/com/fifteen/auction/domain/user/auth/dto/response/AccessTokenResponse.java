package com.fifteen.auction.domain.user.auth.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AccessTokenResponse {

    private final String jwt;
    private final String refreshToken;
}
