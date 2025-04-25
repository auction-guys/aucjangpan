package com.fifteen.auction.domain.product.service;

import com.fifteen.auction.domain.product.entity.Product;
import com.fifteen.auction.domain.product.entity.ProductImage;
import com.fifteen.auction.domain.product.repository.ProductImageRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import com.fifteen.auction.global.dto.exception.ServerException;
import com.fifteen.auction.infra.s3.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final S3Uploader s3Uploader;

    public List<String> upload(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            throw new ClientException(ErrorCode.INVALID_IMAGE_REQUEST);
        }

        List<String> result = new ArrayList<>();
        for (MultipartFile image : images) {
            try {
                String url = s3Uploader.upload(image, "products");
                result.add(url);
            } catch (Exception e) {
                throw new ServerException(ErrorCode.UPLOAD_FAIL);
            }
        }
        return result;
    }

    public void deleteImage(Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ClientException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND));

        s3Uploader.deleteFile(image.getImageUrl());
        productImageRepository.delete(image);
    }

    public void deleteByImageIds(List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) return;

        List<ProductImage> images = productImageRepository.findAllById(imageIds);
        for (ProductImage image : images) {
            s3Uploader.deleteFile(image.getImageUrl());
        }
        productImageRepository.deleteAll(images);
    }

    @Transactional(readOnly = true)
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