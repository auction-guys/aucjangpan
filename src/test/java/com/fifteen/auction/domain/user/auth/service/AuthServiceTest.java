package com.fifteen.auction.domain.user.auth.service;

import com.fifteen.auction.domain.recommend.entity.RecommendGroup;
import com.fifteen.auction.domain.recommend.enums.AgeGroup;
import com.fifteen.auction.domain.recommend.enums.Gender;
import com.fifteen.auction.domain.recommend.enums.Region;
import com.fifteen.auction.domain.recommend.service.RecommendGroupService;
import com.fifteen.auction.domain.recommend.service.RecommendService;
import com.fifteen.auction.domain.user.auth.dto.request.SigninRequest;
import com.fifteen.auction.domain.user.auth.dto.request.SignupRequest;
import com.fifteen.auction.domain.user.auth.dto.request.WithdrawRequest;
import com.fifteen.auction.domain.user.auth.dto.response.AccessTokenResponse;
import com.fifteen.auction.domain.user.auth.dto.response.SigninResponse;
import com.fifteen.auction.domain.user.auth.util.JwtUtil;
import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.domain.user.enums.UserRole;
import com.fifteen.auction.domain.user.repository.UserRepository;
import com.fifteen.auction.global.dto.exception.ClientException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private RecommendGroupService recommendGroupService;

    @Mock
    private RecommendService recommendService;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void 회원가입_성공_시_사용자_저장() {
        // given
        SignupRequest request = new SignupRequest(
                "test@example.com",
                "nickname",
                "홍길동",
                Gender.MALE.name(),
                AgeGroup.TWENTIES.name(),
                "P@ssw0rd!",
                Region.SEOUL.name(),
                "010-1234-5678",
                "패션",
                "123-456-789",
                UserRole.ROLE_USER.name()
        );

        when(userRepository.existsByEmailAndDeletedFalse(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPw");

        RecommendGroup group = mock(RecommendGroup.class);
        when(recommendGroupService.findOrCreate(
                Gender.valueOf(request.getGender()),
                AgeGroup.valueOf(request.getAgeGroup()),
                Region.valueOf(request.getAddress())
        )).thenReturn(group);

        // when
        authService.signup(request);

        // then
        verify(userRepository).save(any(User.class));
    }

    @Test
    void 로그인_성공_시_액세스_토큰과_리프레시_토큰_반환() {
        // given
        SigninRequest request = new SigninRequest("test@example.com", "1234");

        RecommendGroup dummyGroup = RecommendGroup.create(Gender.MALE, AgeGroup.TWENTIES, Region.SEOUL); // 임시 RecommendGroup 객체
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

        // 사용자 ID를 리플렉션으로 강제 주입
        ReflectionTestUtils.setField(user, "id", 1L);

        when(userRepository.findByEmailAndDeletedFalse(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtUtil.createToken(any(), any(), any(), any())).thenReturn("Bearer access-token");
        when(jwtUtil.createRefreshToken(user.getId())).thenReturn("refresh-token");
        when(jwtUtil.substringToken("Bearer access-token")).thenReturn("access-token");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        SigninResponse response = authService.login(request);

        // then
        assertEquals("access-token", response.getJwt());
        assertEquals("refresh-token", response.getRefreshToken());
    }

    @Test
    void 로그인_실패_비밀번호_불일치() {
        // given
        SigninRequest request = new SigninRequest("test@example.com", "wrong");
        User user = new User();
        user.setPassword("encodedPassword");

        when(userRepository.findByEmailAndDeletedFalse(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        // then
        assertThrows(ClientException.class, () -> authService.login(request));
    }

    @Test
    void 로그아웃_성공() {
        // given
        String header = "Bearer valid-token";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(jwtUtil.validateToken("valid-token")).thenReturn(true);
        when(jwtUtil.getTokenExpiration("valid-token")).thenReturn(10000L);
        when(redisTemplate.opsForValue().setIfAbsent(any(), any(), anyLong(), any())).thenReturn(true);

        // when & then
        authService.logout(header);
    }

    @Test
    void 로그아웃_실패_이미_로그아웃된_토큰() {
        // given
        String header = "Bearer token";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(jwtUtil.validateToken("token")).thenReturn(true);
        when(jwtUtil.getTokenExpiration("token")).thenReturn(10000L);
        when(redisTemplate.opsForValue().setIfAbsent(any(), any(), anyLong(), any())).thenReturn(false);

        // then
        assertThrows(ClientException.class, () -> authService.logout(header));
    }

    @Test
    void 회원탈퇴_성공() {

        // given
        WithdrawRequest request = new WithdrawRequest("user@example.com", "1234");

        RecommendGroup dummyGroup = RecommendGroup.create(Gender.MALE, AgeGroup.TWENTIES, Region.SEOUL); // 임시 RecommendGroup 객체
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

        when(userRepository.findByEmailAndDeletedFalse(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);

        // when
        authService.withdraw(request);

        // then
        verify(userRepository).softDeleteByEmail(user.getEmail());
    }

    @Test
    void 회원탈퇴_실패_비밀번호_불일치() {
        // given
        WithdrawRequest request = new WithdrawRequest("user@example.com", "wrong");
        User user = new User();
        user.setPassword("encodedPassword");

        when(userRepository.findByEmailAndDeletedFalse(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        // then
        assertThrows(ClientException.class, () -> authService.withdraw(request));
    }

    @Test
    void 액세스_토큰_재발급() {
        // given
        String refreshToken = "Bearer valid.refresh.token";
        String pureToken = "valid.refresh.token";

        Long userId = 1L;
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn("email@test.com");
        when(user.getNickname()).thenReturn("nick");
        when(user.getRole()).thenReturn(UserRole.ROLE_USER);

        when(jwtUtil.substringToken(refreshToken)).thenReturn(pureToken);
        when(jwtUtil.validateToken(pureToken)).thenReturn(true);
        when(jwtUtil.extractUserId(pureToken)).thenReturn(userId);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("RT:" + userId)).thenReturn(refreshToken);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtUtil.createToken(userId, "email@test.com", "nick", "ROLE_USER")).thenReturn("Bearer new.access.token");
        when(jwtUtil.substringToken("Bearer new.access.token")).thenReturn("new.access.token");

        // when
        AccessTokenResponse response = authService.reissue(refreshToken);

        // then
        assertEquals("new.access.token", response.getJwt());
    }
}
