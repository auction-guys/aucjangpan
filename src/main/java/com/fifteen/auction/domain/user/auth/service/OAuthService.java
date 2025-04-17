package com.fifteen.auction.domain.user.auth.service;

import com.fifteen.auction.domain.recommend.entity.RecommendGroup;
import com.fifteen.auction.domain.recommend.enums.AgeGroup;
import com.fifteen.auction.domain.recommend.enums.Gender;
import com.fifteen.auction.domain.recommend.enums.Region;
import com.fifteen.auction.domain.recommend.service.RecommendGroupService;
import com.fifteen.auction.domain.user.auth.OAuthTokenClient;
import com.fifteen.auction.domain.user.auth.OAuthUserInfoClient;
import com.fifteen.auction.domain.user.auth.dto.response.SigninResponse;
import com.fifteen.auction.domain.user.auth.util.JwtUtil;
import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final OAuthTokenClient tokenClient;
    private final OAuthUserInfoClient infoClient;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RecommendGroupService recommendGroupService;

    @Value("${jwt.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${jwt.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${jwt.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    public SigninResponse loginWithGoogle(String code) {

        String body = String.format(
                "code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=authorization_code",
                code, clientId, clientSecret, redirectUri
        );

        JsonNode tokenNode = tokenClient.getToken(body);
        String accessToken = tokenNode.get("access_token").asText();

        JsonNode userInfo = infoClient.getUserInfo(accessToken);

        String email = userInfo.get("email").asText();
        String name = userInfo.get("name").asText();

        Optional<User> userOpt = userRepository.findByEmailAndDeletedFalse(email);

        User user = userOpt.orElseGet(() -> {

            RecommendGroup group = recommendGroupService.findOrCreate(
                    Gender.MALE, AgeGroup.TWENTIES, Region.SEOUL
            );

            User newUser = new User(
                    email,
                    name,
                    name,
                    "male",
                    "20대",
                    "unknown",
                    "서울특별시 성북구",
                    "010-9876-5432",
                    "설윤 포토카드",
                    "110-353-844210",
                    group
            );
            return userRepository.save(newUser);
        });

        String bearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getNickname());
        String jwt = jwtUtil.substringToken(bearerToken);
        return new SigninResponse(jwt);
    }
}
