package com.fifteen.auction.domain.product.controller;

import com.fifteen.auction.domain.product.dto.response.ProductImageUploadResponse;
import com.fifteen.auction.domain.product.service.ProductImageService;
import com.fifteen.auction.domain.user.auth.entity.AuthUser;
import com.fifteen.auction.global.dto.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products/images")
public class ProductImageController {

    private final ProductImageService productImageService;

    // 이미지 업로드
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<ProductImageUploadResponse>> uploadImages(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam("images") List<MultipartFile> images
    ) {
        List<String> urls = productImageService.upload(images);
        return ResponseEntity.ok(Response.of(ProductImageUploadResponse.of(urls)));
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId) {
        productImageService.deleteImage(imageId);
        return ResponseEntity.ok().build();
    }
}