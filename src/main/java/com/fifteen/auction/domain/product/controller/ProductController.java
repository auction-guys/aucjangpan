package com.fifteen.auction.domain.product.controller;

import com.fifteen.auction.domain.product.dto.request.ProductCreateRequest;
import com.fifteen.auction.domain.product.dto.request.ProductUpdateRequest;
import com.fifteen.auction.domain.product.service.ProductService;
import com.fifteen.auction.domain.user.auth.entity.AuthUser;
import com.fifteen.auction.global.dto.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.fifteen.auction.domain.user.enums.UserRole.Authority.ROLE_USER;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Secured(ROLE_USER)
    @PostMapping
    public ResponseEntity<Response<Long>> createProduct(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody @Valid ProductCreateRequest request
    ) {
        Long productId = productService.createProduct(authUser.getId(), request);
        return ResponseEntity.ok(Response.of(productId));
    }

    @Secured(ROLE_USER)
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody @Valid ProductUpdateRequest request
    ) {
        productService.updateProduct(authUser.getId(), id, request);
        return ResponseEntity.noContent().build();
    }

    @Secured(ROLE_USER)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        productService.deleteProduct(authUser.getId(), id);
        return ResponseEntity.noContent().build();
    }
}