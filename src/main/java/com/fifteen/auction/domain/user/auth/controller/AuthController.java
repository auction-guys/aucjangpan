package com.fifteen.auction.domain.user.auth.controller;

import com.fifteen.auction.domain.user.auth.dto.request.SigninRequest;
import com.fifteen.auction.domain.user.auth.dto.request.SignupRequest;
import com.fifteen.auction.domain.user.auth.dto.request.WithdrawRequest;
import com.fifteen.auction.domain.user.auth.dto.response.SigninResponse;
import com.fifteen.auction.domain.user.auth.service.AuthService;
import com.fifteen.auction.domain.user.auth.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final OAuthService oAuthService;

    @PostMapping("/signup")
    public ResponseEntity<Object> signup(@RequestBody SignupRequest signupRequest) {
        authService.signup(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(Collections.emptyList());
    }

    @PostMapping("/login")
    public ResponseEntity<SigninResponse> login(@RequestBody SigninRequest signinRequest) {
        SigninResponse response = authService.login(signinRequest);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, response.getJwt())
                .build(); // 바디 없이 헤더만 응답
    }

    @PostMapping("/logout")  // 수정
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authorizationHeader) {
        authService.logout(authorizationHeader);  // 헤더 그대로 전달
        return ResponseEntity.ok("로그 아웃이 완료되었습니다.");
    }

    @DeleteMapping("/withdraw")
    public void withdraw(@RequestBody WithdrawRequest withdrawRequest) {
        authService.withdraw(withdrawRequest);
    }

    // 구글 OAuth 로그인 콜백
    @GetMapping("/google/callback")
    public ResponseEntity<SigninResponse> googleCallback(@RequestParam("code") String code) {
        return ResponseEntity.ok(oAuthService.loginWithGoogle(code));
    }
}
