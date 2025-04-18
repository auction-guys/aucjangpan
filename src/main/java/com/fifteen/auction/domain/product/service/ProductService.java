package com.fifteen.auction.domain.product.service;

import com.fifteen.auction.domain.product.dto.request.ProductCreateRequest;
import com.fifteen.auction.domain.product.dto.request.ProductUpdateRequest;
import com.fifteen.auction.domain.product.dto.response.ProductCategoryTreeResponse;
import com.fifteen.auction.domain.product.entity.Product;
import com.fifteen.auction.domain.product.entity.ProductCategory;
import com.fifteen.auction.domain.product.entity.ProductImage;
import com.fifteen.auction.domain.product.repository.ProductCategoryRepository;
import com.fifteen.auction.domain.product.repository.ProductImageRepository;
import com.fifteen.auction.domain.product.repository.ProductRepository;
import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.domain.user.repository.UserRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductImageService productImageService;
    private final ProductCategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public Long createProduct(Long sellerId, ProductCreateRequest request) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ClientException(ErrorCode.USER_NOT_FOUND));

        ProductCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ClientException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));

        Product product = Product.create(seller, category, request.getName(), request.getDescription());

        for (String url : request.getImageUrls()) {
            boolean isThumbnail = url.equals(request.getThumbnailUrl());
            ProductImage image = ProductImage.create(url, product, isThumbnail);
            product.addImage(image);
        }

        return productRepository.save(product).getId();
    }

    public void updateProduct(Long sellerId, Long productId, ProductUpdateRequest request) {
        Product product = productRepository.findByIdWithImages(productId)
                .orElseThrow(() -> new ClientException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!product.getSeller().getId().equals(sellerId)) {
            throw new ClientException(ErrorCode.UNAUTHORIZED);
        }

        ProductCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ClientException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));

        product.update(request.getName(), request.getDescription(), category);

        productImageService.deleteByImageIds(request.getDeleteImageIds());

        List<ProductImage> newImages = productImageService.createImagesWithThumbnail(
                product, request.getImageUrls(), request.getThumbnailUrl());

        for (ProductImage image : newImages) {
            product.addImage(image);
        }
    }

    public void deleteProduct(Long sellerId, Long productId) {
        Product product = productRepository.findByIdWithImages(productId)
                .orElseThrow(() -> new ClientException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!product.getSeller().getId().equals(sellerId)) {
            throw new ClientException(ErrorCode.UNAUTHORIZED);
        }

        product.softDelete();
    }

    private List<ProductCategoryTreeResponse> buildTree(Map<Long, List<ProductCategory>> grouped, Long parentId) {
        return grouped.getOrDefault(parentId, List.of()).stream()
                .map(category -> new ProductCategoryTreeResponse(
                        category.getId(),
                        category.getName(),
                        buildTree(grouped, category.getId())
                ))
                .toList();
    }
}