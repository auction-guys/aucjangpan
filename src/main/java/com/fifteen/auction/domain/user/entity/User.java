package com.fifteen.auction.domain.user.entity;

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

    public User(String email, String nickname, String name, String gender, String ageGroup, String password, String address, String contactNumber, String preferCategory, String accountNumber) {
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
