package com.fifteen.auction.domain.product.controller;

import com.fifteen.auction.domain.product.dto.request.ProductCreateRequest;
import com.fifteen.auction.domain.product.dto.request.ProductUpdateRequest;
import com.fifteen.auction.domain.product.service.ProductService;
import com.fifteen.auction.global.dto.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 등록
     */
    @PostMapping
    public ResponseEntity<Response<Long>> createProduct(
            @RequestHeader("X-USER-ID") Long sellerId,
            @RequestBody @Valid ProductCreateRequest request
    ) {
        Long productId = productService.createProduct(sellerId, request);
        return ResponseEntity.ok(Response.of(productId));
    }

    /**
     * 상품 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateProduct(
            @PathVariable Long id,
            @RequestHeader("X-USER-ID") Long sellerId,
            @RequestBody @Valid ProductUpdateRequest request
    ) {
        productService.updateProduct(sellerId, id, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * 상품 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @RequestHeader("X-USER-ID") Long sellerId
    ) {
        productService.deleteProduct(sellerId, id);
        return ResponseEntity.noContent().build();
    }
}