package com.fifteen.auction.domain.user.auth.service;

import com.fifteen.auction.domain.user.auth.dto.response.SigninResponse;
import com.fifteen.auction.domain.user.auth.util.JwtUtil;
import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.domain.user.repository.UserRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${jwt.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${jwt.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${jwt.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    public SigninResponse loginWithGoogle(String code) {

        RestTemplate restTemplate = new RestTemplate();

        String tokenUri = "https://oauth2.googleapis.com/token";

        // 요청 바디 구성
        String tokenRequestBody = String.format(
                "code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=authorization_code",
                code, clientId, clientSecret, redirectUri
        );

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request = new HttpEntity<>(tokenRequestBody, headers);

        String tokenResponse;
        try {
            // 응답 전체 확인용 코드 (postForEntity 사용)
            ResponseEntity<String> response = restTemplate.postForEntity(
                    tokenUri,
                    request,
                    String.class
            );

            tokenResponse = response.getBody();
        } catch (Exception e) {
            e.printStackTrace(); // 전체 예외 출력
            throw new ClientException(ErrorCode.INVALID_TOKEN);
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode tokenNode;
        try {
            tokenNode = mapper.readTree(tokenResponse);
        } catch (Exception e) {
            throw new ClientException(ErrorCode.INVALID_TOKEN);
        }

        String accessToken = tokenNode.get("access_token").asText();

        // 사용자 정보 요청
        String userInfoUri = "https://www.googleapis.com/oauth2/v3/userinfo";
        String userInfoResponse = restTemplate.getForObject(
                userInfoUri + "?access_token=" + accessToken,
                String.class
        );

        JsonNode userInfo;
        try {
            userInfo = mapper.readTree(userInfoResponse);
        } catch (Exception e) {
            throw new ClientException(ErrorCode.INVALID_TOKEN);
        }

        String email = userInfo.get("email").asText();
        String name = userInfo.get("name").asText();
        String gender = "male"; // 구글 응답에 성별 없음
        String ageGroup = "20대"; // 구글 응답에 생년월인 없음

        Optional<User> userOpt = userRepository.findByEmail(email);

        User user = userOpt.orElseGet(() -> {

            // 회원가입 처리
            User newUser = new User(
                    email,
                    name,
                    name,
                    gender,
                    ageGroup,
                    "unknown",
                    "서울특별시 성북구",
                    "010-9876-5432",
                    "설윤 포토카드",
                    "110-353-844210"
            );

            return userRepository.save(newUser);
        });

        String bearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getNickname());
        String jwt = jwtUtil.substringToken(bearerToken);
        return new SigninResponse(jwt);
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");
        return headers;
    }
}
