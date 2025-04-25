package com.fifteen.auction.domain.user.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fifteen.auction.global.config.FeignConfig;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(
        name = "googleTokenClient",
        url = "https://oauth2.googleapis.com",
        configuration = FeignConfig.class
)
public interface OAuthTokenClient {
    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    JsonNode getToken(MultiValueMap<String, String> body);
}