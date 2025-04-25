package com.fifteen.auction.domain.product.service;

import com.fifteen.auction.domain.product.dto.request.ProductCategorySaveRequest;
import com.fifteen.auction.domain.product.dto.response.ProductCategoryTreeResponse;
import com.fifteen.auction.domain.product.dto.response.ProductCategoryResponse;
import com.fifteen.auction.domain.product.entity.ProductCategory;
import com.fifteen.auction.domain.product.cache.ProductCacheRepository;
import com.fifteen.auction.domain.product.repository.ProductCategoryRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;
    private final ProductCacheRepository productCacheRepository;

    public Long createCategory(ProductCategorySaveRequest request) {
        ProductCategory parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ClientException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));
        }

        ProductCategory category = ProductCategory.create(request.getName(), parent);
        Long id = categoryRepository.save(category).getId();

        // ⬇ 저장 성공 후 캐시 무효화
        productCacheRepository.evictCategoryList();
        productCacheRepository.evictCategoryTree();
        return id;
    }

    @Transactional(readOnly = true)
    public List<ProductCategoryResponse> findAll() {
        List<ProductCategoryResponse> cached = productCacheRepository.getCategoryList();
        if (cached != null) return cached;

        List<ProductCategoryResponse> categories = categoryRepository.findAll().stream()
                .map(c -> ProductCategoryResponse.of(
                        c.getId(),
                        c.getName(),
                        c.getParent() != null ? c.getParent().getId() : null
                ))
                .sorted(Comparator.comparing(ProductCategoryResponse::getName)) // or getSortOrder
                .toList();

        productCacheRepository.saveCategoryList(categories);
        return categories;
    }

    @Transactional(readOnly = true)
    public List<ProductCategoryResponse> findByParentId(Long parentId) {
        return categoryRepository.findAllByParentId(parentId).stream()
                .map(c -> ProductCategoryResponse.of(c.getId(), c.getName(), parentId))
                .collect(Collectors.toList());
    }

    public void softDelete(Long categoryId) {
        ProductCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ClientException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));

        if (category.isDeleted()) {
            throw new ClientException(ErrorCode.CATEGORY_ALREADY_DELETED);
        }

        boolean hasChildren = categoryRepository.existsByParentIdAndDeletedAtIsNull(categoryId);
        if (hasChildren) {
            throw new ClientException(ErrorCode.CATEGORY_DELETE_FORBIDDEN);
        }

        category.softDelete();

        productCacheRepository.evictCategoryList();
        productCacheRepository.evictCategoryTree();
    }

    public void updateCategory(Long categoryId, ProductCategorySaveRequest request) {
        ProductCategory category = categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(() -> new ClientException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));

        ProductCategory newParent = null;
        if (request.getParentId() != null) {
            newParent = categoryRepository.findByIdAndDeletedAtIsNull(request.getParentId())
                    .orElseThrow(() -> new ClientException(ErrorCode.INVALID_CATEGORY_PARENT));
        }

        category.update(request.getName(), newParent);

        productCacheRepository.evictCategoryList();
        productCacheRepository.evictCategoryTree();
    }

    @Transactional(readOnly = true)
    public List<ProductCategoryTreeResponse> getCategoryTree() {
        List<ProductCategoryTreeResponse> cached = productCacheRepository.getCategoryTree();
        if (cached != null) return cached;

        List<ProductCategory> allCategories = categoryRepository.findAllByDeletedAtIsNull();

        Map<Long, List<ProductCategory>> grouped = allCategories.stream()
                .collect(Collectors.groupingBy(c -> c.getParent() != null ? c.getParent().getId() : 0L));

        List<ProductCategoryTreeResponse> tree = buildTree(grouped, 0L);
        productCacheRepository.saveCategoryTree(tree);
        return tree;
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