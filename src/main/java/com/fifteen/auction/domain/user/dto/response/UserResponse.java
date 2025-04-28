package com.fifteen.auction.domain.user.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserResponse {

    private final Long id;

    private final String email;

    private final String nickname;

    private final String preferCategory;
}
