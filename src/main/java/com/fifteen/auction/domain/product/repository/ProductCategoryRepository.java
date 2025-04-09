package com.fifteen.auction.domain.product.repository;

import com.fifteen.auction.domain.product.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    List<ProductCategory> findAllByParentId(Long parentId);

    Optional<ProductCategory> findByName(String name);

    Optional<ProductCategory> findByIdAndDeletedFalse(Long id);

    List<ProductCategory> findAllByDeletedFalse();

    List<ProductCategory> findAllByParentAndDeletedFalse(ProductCategory parent);

    List<ProductCategory> findAllByParentIsNullAndDeletedFalse();
}