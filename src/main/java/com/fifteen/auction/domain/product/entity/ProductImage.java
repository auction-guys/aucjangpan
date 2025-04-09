package com.fifteen.auction.domain.product.entity;

import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import com.fifteen.auction.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private boolean isThumbnail;

    private ProductImage(String imageUrl, boolean isThumbnail, Product product) {
        this.imageUrl = imageUrl;
        this.isThumbnail = isThumbnail;
        this.product = product;
    }

    public static ProductImage create(String imageUrl, Product product, boolean isThumbnail) {
        return new ProductImage(imageUrl, isThumbnail, product);
    }

    public void changeThumbnail(boolean isThumbnail) {
        this.isThumbnail = isThumbnail;
    }
}