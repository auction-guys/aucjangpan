package com.fifteen.auction.global.client.naver;

import com.fifteen.auction.global.client.naver.dto.NaverShoppingItemDto;
import com.fifteen.auction.global.config.NaverFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(
        name = "naverShoppingClient",
        url = "${naver.api.url}",
        configuration = NaverFeignConfig.class
)
public interface NaverSearchClient {

    @GetMapping
    Map<String, Object> searchItems(
            @RequestParam("query") String query,
            @RequestParam("display") int display,
            @RequestParam("sort") String sort
    );
}