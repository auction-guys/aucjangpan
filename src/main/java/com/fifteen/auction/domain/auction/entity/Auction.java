package com.fifteen.auction.domain.auction.entity;

import com.fifteen.auction.domain.product.entity.Product;
import com.fifteen.auction.domain.tag.entity.AuctionTag;
import com.fifteen.auction.domain.tag.entity.Tag;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import com.fifteen.auction.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Auction extends BaseEntity {

    public static final long EXTENSION_TIME = 3L;
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private Long winnerId;

    @Column(nullable = false, length = 10)
    private String auctionSeq;

    @Column(nullable = false)
    private Long startPrice;

    @Column(nullable = false)
    private Long buyNowPrice;

    private Long winPrice;

    @Column(nullable = false)
    private int bidUnit;

    @Column(nullable = false)
    private int views;

    @Column(nullable = false)
    private boolean isBuyNowSet;

    @Column(nullable = false)
    private boolean isAutoExtensible;

    @Enumerated(EnumType.STRING)
    private AuctionStatus status;

    private LocalDateTime doneAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    // Tag 연결을 위해 필요
    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<AuctionTag> tags = new ArrayList<>();

    // 입찰 기록 조회용
    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Bid> bids = new ArrayList<>();

    // 태그 추가 메서드
    public void addTags(List<Tag> tags) {
        for (Tag tag : tags) {
            this.tags.add(AuctionTag.create(this, tag)); // 새로운 태그 추가
        }
    }

    // 태그 삭제 메서드
    public void removeTags(List<Tag> tags) {
        this.tags.removeIf(tag -> tags.contains(tag.getTag())); // 전달된 태그 목록과 일치하는 태그 제거
    }

    public Auction(
            Product product, String auctionSeq, Long startPrice, Long buyNowPrice, int bidUnit,
            boolean isBuyNowSet, boolean isAutoExtensible, LocalDateTime expiresAt
    ) {
        this.product = product;
        this.auctionSeq = auctionSeq;
        this.startPrice = startPrice;
        this.buyNowPrice = isBuyNowSet ? buyNowPrice : 0;
        this.bidUnit = bidUnit;
        this.isBuyNowSet = isBuyNowSet;
        this.isAutoExtensible = isAutoExtensible;
        this.expiresAt = expiresAt;
        this.status = AuctionStatus.PENDING;
        this.views = 0;
    }

    public void open() {
        if (this.status != AuctionStatus.PENDING) {
            throw new ClientException(ErrorCode.AUCTION_ALREADY_OPEN);
        }
        this.status = AuctionStatus.OPEN;
    }

    public void cancel(LocalDateTime doneAt) {
        if (this.status != AuctionStatus.PENDING) {
            throw new ClientException(ErrorCode.CLOSE_NOT_PENDING);
        }
        this.status = AuctionStatus.CANCELED;
        this.doneAt = doneAt;
    }

    public void finalize(Long winnerId, Long winPrice, LocalDateTime doneAt) {
        if (this.status != AuctionStatus.OPEN || this.doneAt != null) {
            throw new ClientException(ErrorCode.FINALIZE_ALREADY_DONE);
        }
        this.winnerId = winnerId;
        this.winPrice = winPrice;
        this.doneAt = doneAt;
        this.status = AuctionStatus.DONE;
    }

    public void misCarry() {
        if (this.status != AuctionStatus.OPEN) {
            throw new ClientException(ErrorCode.AUCTION_NOT_OPEN);
        }
        this.status = AuctionStatus.MISCARRY;
        this.doneAt = this.expiresAt;
    }

    public void extendExpireTime(LocalDateTime bidAt) {
        if (this.isAutoExtensible && is1minBeforeExpiration(bidAt)) {
            this.expiresAt = this.expiresAt.plusMinutes(EXTENSION_TIME);
            this.isAutoExtensible = false; // TODO(yeonic) : 동시성 문제 해결 필요할수도
        }
    }

    public void updateInfo(
            Long startPrice, Long buyNowPrice, Integer bidUnit,
            Boolean isBuyNowSet, Boolean isAutoExtensible
    ) {
        if (this.status != AuctionStatus.PENDING) {
            throw new ClientException(ErrorCode.AUCTION_ALREADY_OPEN);
        }
        this.startPrice = useIfNotNull(startPrice, this.startPrice);
        this.buyNowPrice = useIfNotNull(buyNowPrice, this.buyNowPrice);
        this.bidUnit = useIfNotNull(bidUnit, this.bidUnit);
        this.isBuyNowSet = useIfNotNull(isBuyNowSet, this.isBuyNowSet);
        this.isAutoExtensible = useIfNotNull(isAutoExtensible, this.isAutoExtensible);
    }

    private boolean is1minBeforeExpiration(LocalDateTime bidAt) {
        Duration between = Duration.between(bidAt, this.expiresAt).abs();
        return between.toMinutes() == 0 && between.getSeconds() <= 60;
    }

    private <T> T useIfNotNull(T input, T existing) {
        return input == null ? existing : input;
    }


    // view 추가
    //todo : redis를 통한 view 시간텀을 두고 증가
    public void increaseViews() {
        this.views++;
    }

    // 혹은 전체 tagId 조회용
    public Set<Long> getTagIds() {
        return this.tags.stream()
                .map(at -> at.getTag().getId())
                .collect(Collectors.toSet());
    }
}
