package com.fifteen.auction.domain.product.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fifteen.auction.domain.product.dto.response.ProductCategoryResponse;
import com.fifteen.auction.domain.product.dto.response.ProductCategoryTreeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductCacheRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CATEGORY_ALL_KEY = "product:category:all";
    private static final String CATEGORY_TREE_KEY = "product:category:tree";
    private static final Duration TTL = Duration.ofHours(1);

    public void saveCategoryList(List<ProductCategoryResponse> list) {
        try {
            String json = objectMapper.writeValueAsString(list);
            redisTemplate.opsForValue().set(CATEGORY_ALL_KEY, json, TTL);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("카테고리 리스트 캐싱 실패", e);
        }
    }

    public List<ProductCategoryResponse> getCategoryList() {
        String json = redisTemplate.opsForValue().get(CATEGORY_ALL_KEY);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("카테고리 캐시 파싱 실패", e);
        }
    }

    public void evictCategoryList() {
        redisTemplate.delete(CATEGORY_ALL_KEY);
    }


    public void saveCategoryTree(List<ProductCategoryTreeResponse> tree) {
        try {
            String json = objectMapper.writeValueAsString(tree);
            redisTemplate.opsForValue().set(CATEGORY_TREE_KEY, json, TTL);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("카테고리 트리 캐싱 실패", e);
        }
    }

    public List<ProductCategoryTreeResponse> getCategoryTree() {
        String json = redisTemplate.opsForValue().get(CATEGORY_TREE_KEY);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("카테고리 트리 캐시 파싱 실패", e);
        }
    }

    public void evictCategoryTree() {
        redisTemplate.delete(CATEGORY_TREE_KEY);
    }
}
