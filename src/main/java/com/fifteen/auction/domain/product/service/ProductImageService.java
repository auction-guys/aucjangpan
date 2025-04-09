package com.fifteen.auction.domain.product.service;

import com.fifteen.auction.domain.product.entity.Product;
import com.fifteen.auction.domain.product.entity.ProductImage;
import com.fifteen.auction.domain.product.repository.ProductImageRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import com.fifteen.auction.global.dto.exception.S3UploadException;
import com.fifteen.auction.infra.s3.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final S3Uploader s3Uploader;

    /**
     * ✅ 이미지 업로드 (여러 개)
     */
    public List<String> upload(List<MultipartFile> images) {
        List<String> result = new ArrayList<>();
        for (MultipartFile image : images) {
            try {
                String url = s3Uploader.upload(image, "products");
                result.add(url);
            } catch (Exception e) {
                throw new S3UploadException(ErrorCode.S3_UPLOAD_FAIL, e);
            }
        }
        return result;
    }

    /**
     * ✅ 단일 이미지 삭제 (S3 + DB)
     */
    public void deleteImage(Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ClientException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND));

        s3Uploader.deleteFile(image.getImageUrl());
        productImageRepository.delete(image);
    }

    /**
     * ✅ 여러 이미지 삭제
     */
    public void deleteByImageIds(List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) return;

        List<ProductImage> images = productImageRepository.findAllById(imageIds);
        for (ProductImage image : images) {
            s3Uploader.deleteFile(image.getImageUrl());
        }
        productImageRepository.deleteAll(images);
    }

    /**
     * ✅ 이미지 URL 목록을 ProductImage 리스트로 만들어 반환
     */
    public List<ProductImage> createImagesWithThumbnail(Product product, List<String> imageUrls, String thumbnailUrl) {
        List<ProductImage> result = new ArrayList<>();

        if (imageUrls == null || imageUrls.isEmpty()) return result;

        for (String url : imageUrls) {
            boolean isThumbnail = url.equals(thumbnailUrl);
            ProductImage image = ProductImage.create(url, product, isThumbnail);
            result.add(image);
        }
        return result;
    }
}