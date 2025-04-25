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
            log.info("상품 '{}' 네이버 검색 결과 없음 또는 유효하지 않음", title);
            return List.of();
        }

        String prompt = buildPrompt(title, description, marketSummary);
        return callGptWithPrompt(prompt);
    }

    // 오늘 시세 예측용 메서드
    private String buildPrompt(String title, String description, String marketSummary) {
        LocalDate today = LocalDate.now();
        List<LocalDate> dates = IntStream.rangeClosed(1, 3)
                .mapToObj(i -> today.minusMonths(i).withDayOfMonth(1))
                .collect(Collectors.toList());
        Collections.reverse(dates);
        dates.add(today);

        String exampleJson = buildExampleJsonFormat(dates);

        return String.format("""
                        다음 중고 상품의 오늘(%s)의 예상 거래 가격 범위를 JSON 배열로 정확히 알려줘.
                        
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

    //미래 시세 예측용 메서드
    public List<GPTPricePredictionResponse> callGptForFuturePrices(String title, String description, List<Long> pastWinningPrices) {
        String priceData = pastWinningPrices.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

        String prompt = buildFuturePrompt(title, description, priceData);
        return callGptWithPrompt(prompt);
    }

    //미래 예측용 프롬프트 생성
    private String buildFuturePrompt(String title, String combinedDescriptions, String priceData) {
        LocalDate today = LocalDate.now();
        List<LocalDate> futureDates = IntStream.rangeClosed(1, 3)
                .mapToObj(i -> today.plusMonths(i).withDayOfMonth(1))
                .toList();

        String exampleJson = buildExampleJsonFormat(futureDates);

        return String.format("""
                아래는 최근 중고 상품 "%s"의 낙찰 가격이야: [%s]

                이 데이터와 아래 설명을 바탕으로 다음 3개월(각각 %s, %s, %s)의 중고 거래 가격을 예측해줘.
                **단, 단순 평균이 아닌 가격 추세(상승/하락/유지 등)를 고려해서** 시세를 예측해.
                예: 최근 낙찰가가 점점 낮아지고 있다면 이후 시세도 낮아질 가능성이 높아.
                예: 최근 낙찰가가 고르게 분포되어 있다면 이후 시세도 비슷할 수 있어.
                단순히 평균만 내서 같은 값을 반복하지 마.
                각 달마다 min/max 가격이 조금씩이라도 차이가 나도록 구성해줘.
                (예: 점점 낮아지거나 높아지거나 유지하더라도 약간의 변화는 있어야 해)
               

                상품 설명:
                %s
                응답은 반드시 아래 형식의 JSON 배열로만 반환해야 해.
                숫자에는 쉼표 넣지 말고, 모든 key는 쌍따옴표로 감싸고, 날짜는 yyyy-MM-dd 형식이야.
                %s
                """,
                title,
                priceData,
                futureDates.get(0),
                futureDates.get(1),
                futureDates.get(2),
                combinedDescriptions,
                exampleJson
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