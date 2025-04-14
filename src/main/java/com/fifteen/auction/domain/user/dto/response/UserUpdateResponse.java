package com.fifteen.auction.domain.user.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserUpdateResponse {

    private final Long id;
    private final String email;
    private final String nickname;
    private final String name;
    private final String gender;
    private final String ageGroup;
    private final String address;
    private final String contactNumber;
    private final String preferCategory;
    private final String accountNumber;
}
