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
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private final RecommendGroupService recommendGroupService;
    private final RecommendService recommendService;

    @Transactional
    public void signup(SignupRequest signupRequest) {
        if (userRepository.existsByEmailAndDeletedFalse(signupRequest.getEmail())) {
            throw new ClientException(ErrorCode.DUPLICATE_EMAIL);
        }
        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        // 추천 그룹 찾기 또는 생성
        RecommendGroup group = recommendGroupService.findOrCreate(
                Gender.from(signupRequest.getGender()),
                AgeGroup.from(signupRequest.getAgeGroup()),
                Region.from(signupRequest.getAddress())
        );

        UserRole userRole = Optional.ofNullable(signupRequest.getRole())
                .map(UserRole::valueOf)
                .orElse(UserRole.ROLE_USER); // 기본값 지정

        User user = new User(signupRequest.getEmail(),
                signupRequest.getNickname(),
                signupRequest.getName(),
                Gender.from(signupRequest.getGender()),
                AgeGroup.from(signupRequest.getAgeGroup()),
                encodedPassword,
                Region.from(signupRequest.getAddress()),
                signupRequest.getContactNumber(),
                signupRequest.getPreferCategory(),
                signupRequest.getAccountNumber(),
                group,
                UserRole.valueOf(signupRequest.getRole())
        );
        userRepository.save(user);
        recommendService.generateRecommendationsForGroup(group);
    }

    @Transactional
    public SigninResponse login(SigninRequest signinRequest) {
        User user = userRepository.findByEmailAndDeletedFalse(signinRequest.getEmail()).orElseThrow(
                () -> new ClientException(ErrorCode.USER_NOT_FOUND)
        );

        if (user.getPassword() == null) {
            throw new ClientException(ErrorCode.USER_NOT_PASSWORD_BASED);
        }

        if (!passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())) {
            throw new ClientException(ErrorCode.INVALID_PASSWORD);
        }

        String bearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getNickname(), user.getRole().name());
        String jwt = jwtUtil.substringToken(bearerToken);
        String refreshToken = jwtUtil.createRefreshToken(user.getId()); //리프레쉬 토큰 생성

        // 리프레쉬 토큰 Redis 저장 (key: RT:<userId>)
        redisTemplate.opsForValue().set(
                "RT:" + user.getId(),
                refreshToken,
                jwtUtil.getRefreshTokenExpiry(),
                TimeUnit.MILLISECONDS
        );

        return new SigninResponse(jwt, refreshToken);
    }

    @Transactional
    public void logout(String authorizationHeader) {

        // 1. Authorization 헤더에서 토큰 추출
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            throw new ClientException(ErrorCode.INVALID_TOKEN);
        }
        String token = authorizationHeader.substring(7);

        // 2. 토큰 유효성 검사
        if (!jwtUtil.validateToken(token)) {
            throw new ClientException(ErrorCode.INVALID_TOKEN);
        }

        // 3. Redis에 이미 로그아웃된 토큰인지 확인 + 저장 (원자적으로 처리)
        String key = "BLACKLIST:" + token;
        Long expiration = jwtUtil.getTokenExpiration(token);
        Boolean isSet = redisTemplate.opsForValue().setIfAbsent(key, "logout", expiration, TimeUnit.MILLISECONDS);

        // 4. 이미 존재한다면 → 이미 로그아웃된 토큰
        if (Boolean.FALSE.equals(isSet)) {
            throw new ClientException(ErrorCode.ALREADY_LOGOUT);
        }
    }

    @Transactional
    public void withdraw(WithdrawRequest withdrawRequest) {
        User user = userRepository.findByEmailAndDeletedFalse(withdrawRequest.getEmail()).orElseThrow(
                () -> new ClientException(ErrorCode.USER_NOT_FOUND)
        );

        if (!passwordEncoder.matches(withdrawRequest.getPassword(), user.getPassword())) {
            throw new ClientException(ErrorCode.INVALID_PASSWORD);
        }

        userRepository.softDeleteByEmail(user.getEmail()); // soft-delete 기능 구현
    }

    @Transactional
    public AccessTokenResponse reissue(String refreshToken) {
        // 유효성 검사
        if (!StringUtils.hasText(refreshToken) || !refreshToken.startsWith("Bearer ")) {
            throw new ClientException(ErrorCode.INVALID_TOKEN);
        }

        String pureToken = jwtUtil.substringToken(refreshToken);
        if (!jwtUtil.validateToken(pureToken)) {
            throw new ClientException(ErrorCode.INVALID_TOKEN);
        }

        // 사용자 ID 추출
        Long userId = jwtUtil.extractUserId(pureToken);

        // Redis에 저장된 RefreshToken과 일치하는지 확인
        String redisKey = "RT:" + userId;
        String storedToken = redisTemplate.opsForValue().get(redisKey);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new ClientException(ErrorCode.INVALID_TOKEN);
        }

        // 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ClientException(ErrorCode.USER_NOT_FOUND));

        // AccessToken 재발급
        String newAccessToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getNickname(), user.getRole().name());
        String jwt = jwtUtil.substringToken(newAccessToken);

        return new AccessTokenResponse(jwt);
    }
}