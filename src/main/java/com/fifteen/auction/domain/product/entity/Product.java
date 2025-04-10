package com.fifteen.auction.domain.product.entity;

import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import com.fifteen.auction.global.entity.BaseEntity;
import com.fifteen.auction.domain.product.entity.ProductCategory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ProductCategory category;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ProductImage> images = new ArrayList<>();

    @Column(nullable = false)
    private boolean deleted;

    private LocalDateTime deletedAt;

    private Product(User seller, ProductCategory category, String name, String description) {
        this.seller = seller;
        this.category = category;
        this.name = name;
        this.description = description;
        this.deleted = false;
    }

    public static Product create(User seller, ProductCategory category, String name, String description) {
        return new Product(seller, category, name, description);
    }

    public void update(String name, String description, ProductCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public void addImage(ProductImage image) {
        images.add(image);
    }

    public void softDelete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public ProductImage getThumbnailImage() {
        return images.stream()
                .filter(ProductImage::isThumbnail)
                .findFirst()
                .orElseThrow(() -> new ClientException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND));
    }
}
