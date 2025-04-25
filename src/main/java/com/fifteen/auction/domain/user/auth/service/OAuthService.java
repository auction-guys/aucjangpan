package com.fifteen.auction.domain.user.auth.service;

import com.fifteen.auction.domain.recommend.entity.RecommendGroup;
import com.fifteen.auction.domain.recommend.enums.AgeGroup;
import com.fifteen.auction.domain.recommend.enums.Gender;
import com.fifteen.auction.domain.recommend.enums.Region;
import com.fifteen.auction.domain.recommend.service.RecommendGroupService;
import com.fifteen.auction.domain.user.auth.dto.response.SigninResponse;
import com.fifteen.auction.domain.user.auth.util.JwtUtil;
import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.domain.user.enums.UserRole;
import com.fifteen.auction.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RecommendGroupService recommendGroupService;

    @Value("${jwt.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${jwt.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${jwt.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    public SigninResponse loginWithGoogle(String code) {

        // RestTemplate에 필요한 MessageConverter 설정
        restTemplate.setMessageConverters(Arrays.asList(
                new FormHttpMessageConverter(), // 요청 본문 처리 (application/x-www-form-urlencoded)
                new MappingJackson2HttpMessageConverter() // 응답 처리 (application/json)
        ));

        // 1. Google 토큰 요청
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        JsonNode tokenResponse;
        try {
            tokenResponse = restTemplate.postForObject(
                    "https://oauth2.googleapis.com/token",
                    request,
                    JsonNode.class
            );
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Google OAuth API 호출 중 오류가 발생했습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Google OAuth API 호출 중 오류가 발생했습니다: " + e.getMessage(), e);
        }

        // 2. Access Token으로 사용자 정보 요청
        String accessToken = tokenResponse.get("access_token").asText();
        String refreshToken;
        if (tokenResponse.has("refresh_token")) {
            refreshToken = tokenResponse.get("refresh_token").asText();
        } else {
            throw new RuntimeException("Google API에서 refresh_token을 반환하지 않았습니다.");
        }

        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(accessToken);

        HttpEntity<String> userInfoRequest = new HttpEntity<>(userInfoHeaders);
        JsonNode userInfoResponse;
        try {
            userInfoResponse = restTemplate.postForObject(
                    "https://www.googleapis.com/oauth2/v3/userinfo",
                    userInfoRequest,
                    JsonNode.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Google OAuth 사용자 정보 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }

        // 3. 사용자 정보 추출
        String email = userInfoResponse.get("email").asText();
        String nickname = userInfoResponse.get("name").asText();
        String sub = userInfoResponse.get("sub").asText();

        // 4. RecommendGroup 생성 또는 조회
        RecommendGroup recommendGroup = recommendGroupService.findOrCreate(
                Gender.MALE, AgeGroup.TWENTIES, Region.SEOUL
        );

        // 5. DB에서 사용자 조회 또는 저장
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseGet(() -> {
                    User newUser = new User(
                            email,
                            nickname,
                            nickname,
                            Gender.MALE,
                            AgeGroup.TWENTIES,
                            null,
                            Region.SEOUL,
                            "010-1111-0000",
                            "전자기기",
                            "110-000-000000",
                            recommendGroup,
                            UserRole.ROLE_USER
                    );
                    return userRepository.save(newUser);
                });

        // 6. JWT 생성
        String jwt = jwtUtil.createToken(user.getId(), email, nickname, user.getRole().name());

        // 7. Google의 refresh_token을 그대로 반환
        return new SigninResponse(jwt, refreshToken);
    }
}