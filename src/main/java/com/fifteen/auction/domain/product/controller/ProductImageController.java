package com.fifteen.auction.domain.product.controller;

import com.fifteen.auction.domain.product.dto.request.ProductImageUploadRequest;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.domain.product.dto.response.ProductImageUploadResponse;
import com.fifteen.auction.domain.product.service.ProductImageService;
import com.fifteen.auction.domain.user.auth.entity.AuthUser;
import com.fifteen.auction.global.dto.Response;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

import static com.fifteen.auction.domain.user.enums.UserRole.Authority.ROLE_USER;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products/images")
public class ProductImageController {

    private final ProductImageService productImageService;

    @Secured(ROLE_USER)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<ProductImageUploadResponse>> uploadImages(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam("images") List<MultipartFile> images
    ) {
        log.info(">> 업로드 요청 - 받은 이미지 수: {}", images != null ? images.size() : "null");

        if (images == null || images.isEmpty()) {
            throw new ClientException(ErrorCode.PRODUCT_ACCESS_DENIED);  // 커스텀 예외로 처리
        }

        List<String> urls = productImageService.upload(images);
        return ResponseEntity.ok(Response.of(ProductImageUploadResponse.of(urls)));
    }

    @Secured(ROLE_USER)
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId) {
        productImageService.deleteImage(imageId);
        return ResponseEntity.ok().build();
    }
}