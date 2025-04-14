package com.fifteen.auction.domain.user.auth;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "google-userinfo", url = "https://www.googleapis.com")
public interface OAuthUserInfoClient {
    @GetMapping("/oauth2/v3/userinfo")
    JsonNode getUserInfo(@RequestParam("access_token") String accessToken);
}
