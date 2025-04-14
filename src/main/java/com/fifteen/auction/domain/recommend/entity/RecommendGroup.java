package com.fifteen.auction.domain.recommend.entity;

import com.fifteen.auction.domain.recommend.enums.AgeGroup;
import com.fifteen.auction.domain.recommend.enums.Gender;
import com.fifteen.auction.domain.recommend.enums.Region;
import com.fifteen.auction.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "recommend_group",
        uniqueConstraints = @UniqueConstraint(columnNames = {"gender", "age_group", "region"}))
public class RecommendGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group")
    private AgeGroup ageGroup;

    @Enumerated(EnumType.STRING)
    private Region region;

    public static RecommendGroup create(Gender gender, AgeGroup ageGroup, Region region) {
        RecommendGroup group = new RecommendGroup();
        group.gender = gender;
        group.ageGroup = ageGroup;
        group.region = region;
        return group;
    }
}