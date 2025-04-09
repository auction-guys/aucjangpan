package com.fifteen.auction.domain.product.service;

import com.fifteen.auction.domain.product.dto.request.ProductCategoryCreateRequest;
import com.fifteen.auction.domain.product.dto.request.ProductCategoryTreeResponse;
import com.fifteen.auction.domain.product.dto.request.ProductCategoryUpdateRequest;
import com.fifteen.auction.domain.product.dto.response.ProductCategoryResponse;
import com.fifteen.auction.domain.product.entity.ProductCategory;
import com.fifteen.auction.domain.product.repository.ProductCategoryRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;

    public Long createCategory(ProductCategoryCreateRequest request) {
        ProductCategory parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ClientException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));
        }

        ProductCategory category = ProductCategory.create(request.getName(), parent);
        return categoryRepository.save(category).getId();
    }

    public List<ProductCategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(c -> ProductCategoryResponse.of(c.getId(), c.getName(),
                        c.getParent() != null ? c.getParent().getId() : null))
                .collect(Collectors.toList());
    }

    public List<ProductCategoryResponse> findByParentId(Long parentId) {
        return categoryRepository.findAllByParentId(parentId).stream()
                .map(c -> ProductCategoryResponse.of(c.getId(), c.getName(), parentId))
                .collect(Collectors.toList());
    }
    public void softDelete(Long categoryId) {
        ProductCategory category = categoryRepository.findByIdAndDeletedFalse(categoryId)
                .orElseThrow(() -> new ClientException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));

        category.softDelete(); // deleted = true, deletedAt = now
    }

    public void updateCategory(Long categoryId, ProductCategoryUpdateRequest request) {
        ProductCategory category = categoryRepository.findByIdAndDeletedFalse(categoryId)
                .orElseThrow(() -> new ClientException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));

        ProductCategory newParent = null;
        if (request.getParentId() != null) {
            newParent = categoryRepository.findByIdAndDeletedFalse(request.getParentId())
                    .orElseThrow(() -> new ClientException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));
        }

        category.update(request.getName(), newParent);
    }

    public List<ProductCategoryTreeResponse> getCategoryTree() {
        List<ProductCategory> topCategories = categoryRepository.findAllByParentIsNullAndDeletedFalse();
        return topCategories.stream()
                .map(ProductCategoryTreeResponse::from)
                .collect(Collectors.toList());
    }
}