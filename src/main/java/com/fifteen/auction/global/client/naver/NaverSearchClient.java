package com.fifteen.auction.global.client.naver;

import com.fifteen.auction.global.client.naver.dto.NaverShoppingItemDto;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NaverSearchClient {

    @Value("${naver.api.id}")
    private String clientId;

    @Value("${naver.api.secret}")
    private String clientSecret;

    @Value("${naver.api.url}")
    private String shoppingUrl;

    private final RestTemplate restTemplate = new RestTemplate();

     //네이버 쇼핑 검색 API를 호출하여 상품 검색 결과를 반환
    public List<NaverShoppingItemDto> findShoppingItems(String query) {
        log.info("네이버 쇼핑 API 호출 시작: 검색어 '{}'", query);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);

        URI uri = UriComponentsBuilder
                .fromUriString(shoppingUrl)
                .queryParam("query", query)
                .queryParam("display", 50)
                .queryParam("sort", "sim")
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();

        RequestEntity<Void> requestEntity = new RequestEntity<>(
                headers, HttpMethod.GET, uri
        );

        try {
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    requestEntity,
                    Map.class
            );

            Map<String, Object> responseBody = responseEntity.getBody();
            if (responseBody == null || !responseBody.containsKey("items")) {
                return new ArrayList<>();
            }

            List<Map<String, Object>> items = (List<Map<String, Object>>) responseBody.get("items");
            log.info("네이버 쇼핑 API 검색 성공: 검색어 '{}', 결과 {}개 항목", query, items.size());
            return items.stream()
                    .map(item -> NaverShoppingItemDto.builder()
                            .title((String) item.get("title"))
                            .lprice(parseLongOrDefault((String) item.get("lprice"), 0L))
                            .mallName((String) item.get("mallName"))
                            .build())
                    .toList();
        } catch (Exception e) {
            log.error("네이버 쇼핑 API 호출 실패: 검색어 '{}', 오류: {}", query, e.getMessage(), e);
            throw new ServerException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    private Long parseLongOrDefault(String value, Long defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
