package com.fifteen.auction.domain.product.entity;


import com.fifteen.auction.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /** 상위 카테고리 여부 확장성 고려 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ProductCategory parent;

    private ProductCategory(String name, ProductCategory parent) {
        this.name = name;
        this.parent = parent;
    }

    public static ProductCategory create(String name, ProductCategory parent) {
        return new ProductCategory(name, parent);
    }
}