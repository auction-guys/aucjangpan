package com.fifteen.auction.fixtures;

import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.domain.user.enums.UserRole;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.test.util.ReflectionTestUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserFixture {
    public static User createDefaultSeller(Long userId) {
        User user = new User(
                "seller@example.com",
                "sellery",
                "seller",
                "male",
                "20-29",
                "1234",
                "address",
                "010-1234-5678",
                "electronics",
                "001-1133-111131",
                null,
                UserRole.ROLE_USER
        );
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }
}
