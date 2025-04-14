package com.fifteen.auction.domain.user.entity;

import com.fifteen.auction.domain.recommend.entity.RecommendGroup;
import com.fifteen.auction.domain.recommend.enums.AgeGroup;
import com.fifteen.auction.domain.recommend.enums.Gender;
import com.fifteen.auction.domain.recommend.enums.Region;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String ageGroup;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String address;

    @Column(unique = true, nullable = false)
    private String contactNumber;

    @Column(nullable = false)
    private String preferCategory;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private boolean deleted = false; // 기본값 false

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private RecommendGroup recommendGroup;

    // 수정된 생성자
    public User(String email, String nickname, String name, String gender, String ageGroup, String password, String address, String contactNumber, String preferCategory, String accountNumber, RecommendGroup group) {
        this.email = email;
        this.nickname = nickname;
        this.name = name;
        this.gender = gender;
        this.ageGroup = ageGroup;
        this.password = password;
        this.address = address;
        this.contactNumber = contactNumber;
        this.preferCategory = preferCategory;
        this.accountNumber = accountNumber;
        // 그룹이 null일 경우 create() 메서드로 기본값 생성
        this.recommendGroup = group != null ? group : RecommendGroup.create(Gender.MALE, AgeGroup.TWENTIES, Region.SEOUL);
    }

    public void updateProfile(String email, String nickname, String address) {
        this.email = email;
        this.nickname = nickname;
        this.address = address;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }
}
