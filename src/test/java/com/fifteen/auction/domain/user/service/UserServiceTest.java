package com.fifteen.auction.domain.user.service;

import com.fifteen.auction.domain.recommend.entity.RecommendGroup;
import com.fifteen.auction.domain.recommend.enums.AgeGroup;
import com.fifteen.auction.domain.recommend.enums.Gender;
import com.fifteen.auction.domain.recommend.enums.Region;
import com.fifteen.auction.domain.user.auth.entity.AuthUser;
import com.fifteen.auction.domain.user.dto.request.SetPasswordRequest;
import com.fifteen.auction.domain.user.dto.request.UserUpdatePasswordRequest;
import com.fifteen.auction.domain.user.dto.request.UserUpdateRequest;
import com.fifteen.auction.domain.user.dto.response.UserUpdateResponse;
import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.domain.user.enums.UserRole;
import com.fifteen.auction.domain.user.repository.UserRepository;
import com.fifteen.auction.domain.user.dto.response.UserResponse;
import com.fifteen.auction.global.dto.exception.ClientException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void 사용자_조회_성공() {
        // given
        RecommendGroup dummyGroup = RecommendGroup.create(Gender.MALE, AgeGroup.TWENTIES, Region.SEOUL);
        User user = new User(
                "test@example.com",
                "닉네임",
                "홍길동",
                Gender.MALE,
                AgeGroup.TWENTIES,
                "encodedPassword",
                Region.SEOUL,
                "010-1234-5678",
                "전자기기",
                "1234567890",
                dummyGroup,
                UserRole.ROLE_USER
        );

        ReflectionTestUtils.setField(user, "id", 1L);

        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(user));

        // when
        UserResponse response = userService.findUser(1L);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getNickname()).isEqualTo("닉네임");
    }


    @Test
    void 사용자_조회_실패() {
        // given
        lenient().when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findUser(999L))
                .isInstanceOf(ClientException.class);
    }

    @Test
    void 사용자_프로필_수정_성공() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@example.com", "ROLE_USER");

        RecommendGroup dummyGroup = RecommendGroup.create(Gender.MALE, AgeGroup.TWENTIES, Region.SEOUL);
        User user = new User(
                "test@example.com", "닉네임", "홍길동",
                Gender.MALE, AgeGroup.TWENTIES, "encodedPassword",
                Region.SEOUL, "010-1234-5678", "전자기기", "1234567890",
                dummyGroup, UserRole.ROLE_USER
        );

        UserUpdateRequest request = new UserUpdateRequest("new@example.com", "새닉네임", "BUSAN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByNickname("새닉네임")).thenReturn(false);

        // when
        UserUpdateResponse response = userService.updateProfile(authUser, request);

        // then
        assertThat(response.getEmail()).isEqualTo("new@example.com");
        assertThat(response.getNickname()).isEqualTo("새닉네임");
        assertThat(response.getAddress()).isEqualTo("BUSAN");
    }

    @Test
    void 사용자_비밀번호_변경_성공() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@email.com", "ROLE_USER");

        RecommendGroup dummyGroup = RecommendGroup.create(Gender.MALE, AgeGroup.TWENTIES, Region.SEOUL);
        User user = new User(
                "test@email.com", "닉네임", "홍길동",
                Gender.MALE, AgeGroup.TWENTIES, "encodedOldPw",
                Region.SEOUL, "010-1234-5678", "전자기기", "1234567890",
                dummyGroup, UserRole.ROLE_USER
        );

        UserUpdatePasswordRequest request = new UserUpdatePasswordRequest("oldPw", "newPw");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPw", "encodedOldPw")).thenReturn(true);
        when(passwordEncoder.matches("newPw", "encodedOldPw")).thenReturn(false);
        when(passwordEncoder.encode("newPw")).thenReturn("encodedNewPw");

        // when
        userService.updatePassword(authUser, request);

        // then
        assertThat(user.getPassword()).isEqualTo("encodedNewPw");
    }

    @Test
    void 사용자_비밀번호_설정_성공() {
        // given
        RecommendGroup dummyGroup = RecommendGroup.create(Gender.MALE, AgeGroup.TWENTIES, Region.SEOUL);
        User user = new User(
                "test@example.com", "닉네임", "홍길동",
                Gender.MALE, AgeGroup.TWENTIES, null,
                Region.SEOUL, "010-1234-5678", "전자기기", "1234567890",
                dummyGroup, UserRole.ROLE_USER
        );

        SetPasswordRequest request = new SetPasswordRequest("새비밀번호", "새비밀번호");

        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("새비밀번호")).thenReturn("encodedNewPassword");

        // when
        userService.setPassword(1L, request);

        // then
        assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
    }

    @Test
    void 비밀번호_초기_설정_실패_비밀번호_불일치() {
        // given
        SetPasswordRequest request = new SetPasswordRequest("pw1234", "different");

        // when & then
        assertThatThrownBy(() -> userService.setPassword(1L, request))
                .isInstanceOf(ClientException.class)
                .hasMessage(null);
    }

    @Test
    void 비밀번호_초기_설정_실패_이미_존재하는_비밀번호() {
        // given
        SetPasswordRequest request = new SetPasswordRequest("pw1234", "pw1234");
        User user = mock(User.class);

        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(user));
        when(user.getPassword()).thenReturn("existingPassword");

        // when & then
        assertThatThrownBy(() -> userService.setPassword(1L, request))
                .isInstanceOf(ClientException.class)
                .hasMessage(null);
    }

}
