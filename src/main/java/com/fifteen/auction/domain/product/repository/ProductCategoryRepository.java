package com.fifteen.auction.domain.product.repository;

import com.fifteen.auction.domain.product.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    Optional<ProductCategory> findByIdAndDeletedAtIsNull(Long id);

    List<ProductCategory> findAllByParentIsNullAndDeletedAtIsNull();

    List<ProductCategory> findAllByParentId(Long parentId);

    List<ProductCategory> findAllByDeletedAtIsNull();

    // 보류 상태
    // Optional<ProductCategory> findByName(String name);
}