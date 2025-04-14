package com.fifteen.auction.domain.user.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignupRequest {

    @NotBlank
    @Email(message = "이메일 형식이어야 합니다.")
    private String email;

    @NotBlank
    private String nickname;

    @NotBlank
    private String name;

    @NotBlank
    private String gender;

    @NotBlank
    private String ageGroup;

    @NotBlank
    @Size(min = 8, max = 24, message = "비밀번호는 8자 이상 24자 이하이어야 합니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{8,24}$", message = "비밀번호는 영어 대소문자, 숫자, 특수문자를 포함해야 합니다.")
    private String password;

    @NotBlank
    private String address;

    @NotBlank
    private String contactNumber;

    @NotBlank
    private String preferCategory;

    @NotBlank
    private String accountNumber;
}
