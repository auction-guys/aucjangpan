package com.fifteen.auction.domain.product.service;

import com.fifteen.auction.domain.product.dto.request.ProductCategorySaveRequest;
import com.fifteen.auction.domain.product.dto.response.ProductCategoryTreeResponse;
import com.fifteen.auction.domain.product.dto.response.ProductCategoryResponse;
import com.fifteen.auction.domain.product.entity.ProductCategory;
import com.fifteen.auction.domain.product.repository.ProductCategoryRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;

    public Long createCategory(ProductCategorySaveRequest request) {
        ProductCategory parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ClientException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));
        }

        ProductCategory category = ProductCategory.create(request.getName(), parent);
        return categoryRepository.save(category).getId();
    }

    @Transactional(readOnly = true)
    public List<ProductCategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(c -> ProductCategoryResponse.of(c.getId(), c.getName(),
                        c.getParent() != null ? c.getParent().getId() : null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductCategoryResponse> findByParentId(Long parentId) {
        return categoryRepository.findAllByParentId(parentId).stream()
                .map(c -> ProductCategoryResponse.of(c.getId(), c.getName(), parentId))
                .collect(Collectors.toList());
    }
    public void softDelete(Long categoryId) {
        ProductCategory category = categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(() -> new ClientException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));

        category.softDelete(); // deleted = true, deletedAt = now
    }

    public void updateCategory(Long categoryId, ProductCategorySaveRequest request) {
        ProductCategory category = categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(() -> new ClientException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));

        ProductCategory newParent = null;
        if (request.getParentId() != null) {
            newParent = categoryRepository.findByIdAndDeletedAtIsNull(request.getParentId())
                    .orElseThrow(() -> new ClientException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));
        }

        category.update(request.getName(), newParent);
    }

    @Transactional(readOnly = true)
    public List<ProductCategoryTreeResponse> getCategoryTree() {
        List<ProductCategory> allCategories = categoryRepository.findAllByDeletedAtIsNull();

        // 1. parentId 기준으로 그룹핑 (null은 0L로 처리)
        Map<Long, List<ProductCategory>> grouped = allCategories.stream()
                .collect(Collectors.groupingBy(c -> c.getParent() != null ? c.getParent().getId() : 0L));

        // 2. 트리 구성 재귀 함수 실행
        return buildTree(grouped, 0L);
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