package com.fifteen.auction.domain.user.auth.service;

import com.fifteen.auction.domain.user.auth.dto.request.SigninRequest;
import com.fifteen.auction.domain.user.auth.dto.request.SignupRequest;
import com.fifteen.auction.domain.user.auth.dto.request.WithdrawRequest;
import com.fifteen.auction.domain.user.auth.dto.response.SigninResponse;
import com.fifteen.auction.domain.user.auth.util.JwtUtil;
import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.domain.user.repository.UserRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public String signup(SignupRequest signupRequest) {

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new ClientException(ErrorCode.DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        User user = new User(signupRequest.getEmail(),
                             signupRequest.getNickname(),
                             signupRequest.getName(),
                             signupRequest.getGender(),
                             signupRequest.getAgeGroup(),
                             encodedPassword,
                             signupRequest.getAddress(),
                             signupRequest.getContactNumber(),
                             signupRequest.getPreferCategory(),
                             signupRequest.getAccountNumber());

        userRepository.save(user);

        return "회원가입이 완료되었습니다";
    }

    @Transactional
    public SigninResponse login(SigninRequest signinRequest) {

        User user = userRepository.findByEmail(signinRequest.getEmail()).orElseThrow(
                () -> new ClientException(ErrorCode.USER_NOT_FOUND)
        );

        if (!passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())) {
            throw new ClientException(ErrorCode.INVALID_PASSWORD);
        }

        String bearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getNickname());
        String jwt = jwtUtil.substringToken(bearerToken);

        return new SigninResponse(jwt);
    }

    @Transactional
    public void logout(String authorizationHeader) {

        // Authorization 헤더에서 토큰 추출
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            throw new ClientException(ErrorCode.INVALID_TOKEN);
        }
        String token = authorizationHeader.substring(7);

        // 토큰 유효성 검사
        if (!jwtUtil.validateToken(token)) {
            throw new ClientException(ErrorCode.INVALID_TOKEN);
        }

        String isBlacklisted = redisTemplate.opsForValue().get("BLACKLIST:" + token);
        if ("logout".equals(isBlacklisted)) {
            throw new ClientException(ErrorCode.ALREADY_LOGOUT);  // 커스텀 예외 코드 사용
        }

        // Redis에 블랙리스트 등록
        Long expiration = jwtUtil.getTokenExpiration(token);
        redisTemplate.opsForValue().set("BLACKLIST:" + token, "logout", expiration, TimeUnit.MILLISECONDS);
    }

    @Transactional
    public void withdraw(WithdrawRequest withdrawRequest) {
        User user = userRepository.findByEmail(withdrawRequest.getEmail()).orElseThrow(
                () -> new ClientException(ErrorCode.USER_NOT_FOUND)
        );

        if (!passwordEncoder.matches(withdrawRequest.getPassword(), user.getPassword())) {
            throw new ClientException(ErrorCode.INVALID_PASSWORD);
        }

        userRepository.delete(user);
    }
}
