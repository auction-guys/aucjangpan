package com.fifteen.auction.global.client.chatgpt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fifteen.auction.domain.product.dto.response.GPTPricePredictionResponse;
import com.fifteen.auction.global.client.chatgpt.dto.ChatGPTRequest;
import com.fifteen.auction.global.client.chatgpt.dto.ChatMessage;
import com.fifteen.auction.global.client.naver.dto.NaverShoppingItemDto;
import com.fifteen.auction.global.client.naver.NaverSearchClient;
import com.fifteen.auction.global.client.naver.dto.NaverShoppingSearchResponse;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatGPTClient {

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.api.key}")
    private String apiKey;

    private final NaverSearchClient naverSearchClient;

    public List<GPTPricePredictionResponse> callGptForHistoricalPrices(String title, String description) {
        log.info("상품 '{}' 시세 예측 시작 - 네이버 쇼핑 API 호출", title);

        NaverShoppingSearchResponse response = naverSearchClient.searchItems(title, 50, "sim");
        List<NaverShoppingItemDto> allItems = response.getItems();

        List<NaverShoppingItemDto> catalogItems = allItems.stream()
                .filter(item ->
                        (item.getProductId() != null && !item.getProductId().isEmpty()) ||
                                (item.getLink() != null && item.getLink().contains("/catalog/"))
                )
                .toList();

        // 카탈로그상품 없을경우 -> 일반상품
        List<NaverShoppingItemDto> fallbackItems = allItems.stream()
                .filter(item ->
                        (item.getProductId() == null || item.getProductId().isEmpty()) &&
                                (item.getLink() == null || !item.getLink().contains("/catalog/"))
                )
                .toList();

        List<NaverShoppingItemDto> shoppingItems = !catalogItems.isEmpty() ? catalogItems : fallbackItems;

        String marketSummary = null;

        if (!shoppingItems.isEmpty()) {
            log.info("상품 '{}' 네이버 검색 결과 {}개 항목 - GPT 예측 시작", title, shoppingItems.size());

            long minPrice = Long.MAX_VALUE;
            long maxPrice = 0L;
            long totalPrice = 0L;
            int validItemCount = 0;

            StringBuilder summary = new StringBuilder();
            summary.append("현재 최신 시장 데이터 (네이버 쇼핑):\n");

            for (NaverShoppingItemDto item : shoppingItems) {
                if (item.getLprice() != null && item.getLprice() > 0) {
                    minPrice = Math.min(minPrice, item.getLprice());
                    maxPrice = Math.max(maxPrice, item.getLprice());
                    totalPrice += item.getLprice();
                    validItemCount++;

                    summary.append(String.format("- %s: %,d원 (판매처: %s)\n",
                            item.getTitle().replaceAll("<[^>]*>", ""),
                            item.getLprice(),
                            item.getMallName()));

                    if (validItemCount >= 10) break;
                }
            }

            if (validItemCount > 0) {
                summary.append(String.format("\n현재 최저가: %,d원\n", minPrice));
                summary.append(String.format("현재 최고가: %,d원\n", maxPrice));
                summary.append(String.format("현재 평균가: %,d원\n", totalPrice / validItemCount));
                marketSummary = summary.toString();
            }
        }

        if (shoppingItems.isEmpty() || marketSummary == null) {
            log.info("상품 '{}' 네이버 검색 결과 없음 또는 유효하지 않음 - ChatGPT를 사용한 예측으로 전환", title);
        }

        String prompt = buildPrompt(title, description, marketSummary);
        return callGptWithPrompt(prompt);
    }

    private String buildPrompt(String title, String description, String marketSummary) {
        LocalDate today = LocalDate.now();
        List<LocalDate> dates = IntStream.rangeClosed(1, 3)
                .mapToObj(i -> today.minusMonths(i).withDayOfMonth(1))
                .collect(Collectors.toList());
        Collections.reverse(dates);
        dates.add(today);

        String exampleJson = buildExampleJsonFormat(dates);

        return String.format("""
                        다음 중고 상품의 최근 3개월 (1일 기준)과 오늘(%s)의 예상 거래 가격 범위를 JSON 배열로 정확히 알려줘.
                        
                        반드시 아래 조건을 지켜야해
                        
                        숫자에는 쉼표(,)를 절대 넣지 마 -> 예: 100800 (o), 100,800 (x)
                        숫자는 정수형태로만해
                        모든 key는 반드시 쌍따옴표 감싸
                        %s
                        
                        %s
                        제품명: %s
                        설명: %s
                        """,
                today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                exampleJson,
                marketSummary != null ? marketSummary + "\n" : "",
                title,
                description
        );
    }

    private String buildExampleJsonFormat(List<LocalDate> dates) {
        StringBuilder sb = new StringBuilder("[\n");
        for (LocalDate date : dates) {
            sb.append(String.format("  { \"date\": \"%s\", \"min\": 최저가, \"max\": 최고가 },\n",
                    date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        }
        sb.setLength(sb.length() - 2); // 마지막 쉼표 제거
        sb.append("\n]");
        return sb.toString();
    }

    private List<GPTPricePredictionResponse> callGptWithPrompt(String prompt) {
        ChatMessage userMessage = new ChatMessage("user", prompt);
        ChatGPTRequest request = ChatGPTRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(userMessage))
                .temperature(0.5)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<ChatGPTRequest> entity = new HttpEntity<>(request, headers);

        ObjectMapper objectMapper = new ObjectMapper();
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            String content = extractGptContent(objectMapper, response.getBody());

            try {
                return objectMapper.readValue(
                        content,
                        new TypeReference<>() {
                        }
                );
            } catch (Exception e) {
                log.warn("GPT 응답 JSON 파싱 실패 - 재시도 시도함");

                ChatMessage retryMessage = new ChatMessage("user", prompt + "\nJSON 형식이 올바르지 않았습니다. 정확한 JSON 형식으로 다시 응답해 주세요.");
                ChatGPTRequest retryRequest = ChatGPTRequest.builder()
                        .model("gpt-3.5-turbo")
                        .messages(List.of(retryMessage))
                        .temperature(0.5)
                        .build();

                ResponseEntity<String> retryResponse = restTemplate.exchange(
                        apiUrl,
                        HttpMethod.POST,
                        new HttpEntity<>(retryRequest, headers),
                        String.class
                );

                String retryContent = extractGptContent(objectMapper, retryResponse.getBody());

                try {
                    return objectMapper.readValue(retryContent, new TypeReference<>() {
                    });
                } catch (Exception retryFail) {
                    log.error("GPT 재시도 응답 파싱 실패 - 응답 내용:\n{}", retryContent, retryFail);
                    throw new ServerException(ErrorCode.EXTERNAL_API_ERROR);
                }
            }

        } catch (Exception e) {
            log.error("GPT API 호출 실패", e);
            throw new ServerException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    private String extractGptContent(ObjectMapper objectMapper, String responseBody) throws Exception {
        return objectMapper
                .readTree(responseBody)
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();
    }
}