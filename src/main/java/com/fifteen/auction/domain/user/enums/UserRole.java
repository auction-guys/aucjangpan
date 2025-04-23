package com.fifteen.auction.domain.user.enums;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

public enum UserRole {
    ROLE_USER,
    ROLE_ADMIN;

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static abstract class Authority {
        public static final String ROLE_USER = "ROLE_USER";
        public static final String ROLE_ADMIN = "ROLE_ADMIN";
    }
}
